from sqlalchemy.orm import Session

from models.users import User
from models.category import Category
from models.settings import UserSettings
from models.transaction_source import TransactionSource
import json


DEFAULT_CATEGORIES = [
    # Expense categories
    {
        "category_name": "Food",
        "category_type": "expense"
    },
    {
        "category_name": "Shopping",
        "category_type": "expense"
    },
    {
        "category_name": "Transport",
        "category_type": "expense"
    },
    {
        "category_name": "Bills",
        "category_type": "expense"
    },
    {
        "category_name": "Entertainment",
        "category_type": "expense"
    },
    {
        "category_name": "Health",
        "category_type": "expense"
    },
    {
        "category_name": "Education",
        "category_type": "expense"
    },
    {
        "category_name": "Other",
        "category_type": "expense"
    },

    # Income categories
    {
        "category_name": "Salary",
        "category_type": "income"
    },
    {
        "category_name": "Freelance",
        "category_type": "income"
    },
    {
        "category_name": "Business",
        "category_type": "income"
    },
    {
        "category_name": "Investment",
        "category_type": "income"
    },
    {
        "category_name": "Gift",
        "category_type": "income"
    },
    {
        "category_name": "Other",
        "category_type": "income"
    }
]


def get_or_create_user(
        db: Session,
        name: str,
        email: str,
        google_id: str
):
    user = db.query(User).filter(
        User.email == email
    ).first()

    # Existing user
    if user:
        return user

    # Create new user
    user = User(
        name=name,
        email=email,
        google_id=google_id
    )

    db.add(user)
    db.flush()

    # Create default settings.
    db.add(
        UserSettings(
            user_id=user.id,
            notifications_enabled=True,
            sms_detection_enabled=True,
            detection_mode="APPROVAL"
        )
    )

    # Create the built-in SMS source. The empty sender list means that
    # Android should inspect all incoming SMS and let the transaction parser
    # decide whether a message is financial.
    db.add(
        TransactionSource(
            user_id=user.id,
            app_name="SMS (Default)",
            package_name=None,
            source_type="SMS",
            sender_ids=json.dumps([]),
            enabled=True
        )
    )

    # Create default categories for the new user
    for category_data in DEFAULT_CATEGORIES:

        category = Category(
            user_id=user.id,
            category_name=category_data["category_name"],
            category_type=category_data["category_type"]
        )

        db.add(category)

    db.commit()
    db.refresh(user)

    return user