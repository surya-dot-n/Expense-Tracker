from sqlalchemy.orm import Session
import re

from .schemas import SMSCreate, TransactionExtracted


def save_sms(
    db: Session,
    sms_data: SMSCreate,
    user_id: int
):

    from models.sms import SMS

    sms = SMS(
        user_id=user_id,
        sender=sms_data.sender,
        message=sms_data.message,
        received_at=sms_data.received_at
    )

    db.add(sms)
    db.commit()
    db.refresh(sms)

    return sms


def get_all_sms(
    db: Session,
    user_id: int
):

    from models.sms import SMS

    return (
        db.query(SMS)
        .filter(
            SMS.user_id == user_id
        )
        .order_by(
            SMS.received_at.desc()
        )
        .all()
    )


def get_sms_by_id(
    db: Session,
    sms_id: int,
    user_id: int
):

    from models.sms import SMS

    return (
        db.query(SMS)
        .filter(
            SMS.id == sms_id,
            SMS.user_id == user_id
        )
        .first()
    )


def extract_transaction(
    message: str | SMSCreate
) -> TransactionExtracted:

    # The service is used by both the API (which passes the message text)
    # and tests/older callers (which may pass SMSCreate).
    if isinstance(message, SMSCreate):
        message = message.message

    # --------------------------------
    # 1. Extract amount
    # --------------------------------

    amount = None

    amount_patterns = [
        r'(?:Rs\.?|INR|₹)\s*([\d,]+(?:\.\d{1,2})?)',
        r'([\d,]+(?:\.\d{1,2})?)\s*(?:Rs\.?|INR|₹)'
    ]

    for pattern in amount_patterns:

        match = re.search(
            pattern,
            message,
            re.IGNORECASE
        )

        if match:

            amount = float(
                match.group(1).replace(",", "")
            )

            break

    # --------------------------------
    # 2. Detect transaction type
    # --------------------------------

    transaction_type = None

    lower_message = message.lower()

    debit_words = [
        "sent",
        "debited",
        "debit",
        "spent",
        "paid",
        "payment",
        "withdrawn"
    ]

    credit_words = [
        "credited",
        "credit",
        "received",
        "deposited"
    ]

    if any(
        word in lower_message
        for word in debit_words
    ):

        transaction_type = "Expense"

    elif any(
        word in lower_message
        for word in credit_words
    ):

        transaction_type = "Income"

    # --------------------------------
    # 3. Extract merchant
    # --------------------------------

    merchant = None

    merchant_patterns = [
        r'at\s+([A-Za-z0-9 .&_-]+)',
        r'to\s+([A-Za-z0-9 .&_-]+)',
        r'from\s+([A-Za-z0-9 .&_-]+)'
    ]

    for pattern in merchant_patterns:

        match = re.search(
            pattern,
            message,
            re.IGNORECASE
        )

        if match:

            merchant = match.group(1).strip()

            merchant = re.split(
                r'\s+(?:on|using|via|ref|txn|transaction)\b',
                merchant,
                flags=re.IGNORECASE
            )[0].strip()

            break

    # --------------------------------
    # 4. Description
    # --------------------------------

    description = message

    return TransactionExtracted(
        amount=amount,
        transaction_type=transaction_type,
        merchant=merchant,
        description=description
    )


def find_category(
    db: Session,
    user_id: int,
    merchant: str | None,
    transaction_type: str
):

    from models.category import Category

    categories = (
        db.query(Category)
        .filter(
            Category.user_id == user_id
        )
        .all()
    )

    if not categories:
        return None

    if merchant:

        merchant_lower = merchant.lower()

        category_keywords = {

            "food": [
                "swiggy",
                "zomato",
                "restaurant",
                "hotel",
                "food"
            ],

            "shopping": [
                "amazon",
                "flipkart",
                "myntra",
                "shopping"
            ],

            "travel": [
                "uber",
                "ola",
                "rapido",
                "irctc"
            ],

            "entertainment": [
                "netflix",
                "spotify",
                "movie",
                "bookmyshow"
            ]
        }

        for category_name, keywords in category_keywords.items():

            for keyword in keywords:

                if keyword in merchant_lower:

                    for category in categories:

                        if (
                            category.category_name.lower()
                            == category_name
                            and category.category_type.lower()
                            == transaction_type
                        ):

                            return category

    return None


def create_transaction_from_sms(
    db: Session,
    sms_id: int,
    user_id: int
):
    from models.sms import SMS
    from models.transaction import Transaction

    # Get the SMS
    sms = (
        db.query(SMS)
        .filter(
            SMS.id == sms_id,
            SMS.user_id == user_id
        )
        .first()
    )

    if not sms:
        return None, "SMS not found"

    # Extract transaction information
    extracted = extract_transaction(sms.message)

    # Check amount
    if extracted.amount is None:
        return None, "Could not extract transaction amount"

    # Check transaction type
    if extracted.transaction_type is None:
        return None, "Could not determine transaction type"

    # Find category
    category = find_category(
        db=db,
        user_id=user_id,
        merchant=extracted.merchant,
        transaction_type=extracted.transaction_type
    )

    if not category:
        return None, "No matching category found"

    # Create transaction
    transaction = Transaction(
        user_id=user_id,
        category_id=category.id,
        amount=extracted.amount,
        description=extracted.description,
        transaction_type=extracted.transaction_type
    )

    db.add(transaction)
    db.commit()
    db.refresh(transaction)

    return transaction, None