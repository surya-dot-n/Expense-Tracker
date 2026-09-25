from sqlalchemy.orm import Session

from models.pending_transaction import PendingTransaction


def get_pending_transactions(
    db: Session,
    user_id: int
):
    return (
        db.query(PendingTransaction)
        .filter(
            PendingTransaction.user_id == user_id,
            PendingTransaction.status == "PENDING"
        )
        .order_by(
            PendingTransaction.detected_at.desc()
        )
        .all()
    )


def get_pending_transaction(
    db: Session,
    user_id: int,
    pending_id: int
):
    return (
        db.query(PendingTransaction)
        .filter(
            PendingTransaction.id == pending_id,
            PendingTransaction.user_id == user_id
        )
        .first()
    )


def create_pending_transaction(
    db: Session,
    user_id: int,
    source_id: int | None,
    app_name: str | None,
    package_name: str | None,
    source_type: str,
    sender_id: str | None,
    message_body: str | None,
    amount,
    transaction_type: str,
    description: str | None,
    category_id: int | None,
    transaction_date,
    confidence,
    fingerprint: str | None
):
    # --------------------------------------------------------
    # Duplicate protection
    # --------------------------------------------------------

    if fingerprint:

        existing = (
            db.query(PendingTransaction)
            .filter(
                PendingTransaction.user_id == user_id,
                PendingTransaction.fingerprint == fingerprint
            )
            .first()
        )

        if existing:
            return existing

    pending = PendingTransaction(
        user_id=user_id,
        source_id=source_id,
        app_name=app_name,
        package_name=package_name,
        source_type=source_type,
        sender_id=sender_id,
        message_body=message_body,
        amount=amount,
        transaction_type=transaction_type,
        description=description,
        category_id=category_id,
        transaction_date=transaction_date,
        confidence=confidence,
        fingerprint=fingerprint,
        status="PENDING"
    )

    db.add(pending)

    db.commit()

    db.refresh(pending)

    return pending


# ============================================================
# UPDATE CATEGORY
# ============================================================

def update_pending_transaction_category(
    db: Session,
    user_id: int,
    pending_id: int,
    category_id: int
):
    pending = get_pending_transaction(
        db=db,
        user_id=user_id,
        pending_id=pending_id
    )

    if not pending:
        raise ValueError(
            "Pending transaction not found."
        )

    if pending.status != "PENDING":
        raise ValueError(
            "This pending transaction has already been processed."
        )

    pending.category_id = category_id

    db.commit()

    db.refresh(pending)

    return pending


# ============================================================
# APPROVE
# ============================================================

def approve_pending_transaction(
    db: Session,
    user_id: int,
    pending_id: int
):
    pending = get_pending_transaction(
        db=db,
        user_id=user_id,
        pending_id=pending_id
    )

    if not pending:
        raise ValueError(
            "Pending transaction not found."
        )

    if pending.status != "PENDING":
        raise ValueError(
            "This pending transaction has already been processed."
        )

    if pending.category_id is None:
        raise ValueError(
            "Category is required before approving this transaction."
        )

    from models.transaction import Transaction

    # --------------------------------------------------------
    # Convert pending transaction date/time
    # --------------------------------------------------------

    transaction_date = None
    transaction_time = None

    if pending.transaction_date is not None:

        transaction_date = pending.transaction_date.date()

        transaction_time = pending.transaction_date.time()

        if (
            transaction_time.hour == 0
            and transaction_time.minute == 0
            and transaction_time.second == 0
            and transaction_time.microsecond == 0
        ):
            transaction_time = None

    # --------------------------------------------------------
    # Create actual transaction
    # --------------------------------------------------------

    transaction = Transaction(
        user_id=user_id,
        category_id=pending.category_id,
        amount=pending.amount,
        description=pending.description,
        transaction_type=pending.transaction_type,
        transaction_date=transaction_date,
        transaction_time=transaction_time
    )

    db.add(transaction)

    pending.status = "APPROVED"

    db.commit()

    db.refresh(transaction)

    db.refresh(pending)

    return transaction, pending


# ============================================================
# DENY
# ============================================================

def deny_pending_transaction(
    db: Session,
    user_id: int,
    pending_id: int
):
    pending = get_pending_transaction(
        db=db,
        user_id=user_id,
        pending_id=pending_id
    )

    if not pending:
        raise ValueError(
            "Pending transaction not found."
        )

    if pending.status != "PENDING":
        raise ValueError(
            "This pending transaction has already been processed."
        )

    pending.status = "DENIED"

    db.commit()

    db.refresh(pending)

    return pending


# ============================================================
# DELETE
# ============================================================

def delete_pending_transaction(
    db: Session,
    user_id: int,
    pending_id: int
):
    pending = get_pending_transaction(
        db=db,
        user_id=user_id,
        pending_id=pending_id
    )

    if not pending:
        raise ValueError(
            "Pending transaction not found."
        )

    db.delete(pending)

    db.commit()