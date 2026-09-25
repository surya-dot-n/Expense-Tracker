from sqlalchemy.orm import Session

from .schemas import TransactionCreate
from models.transaction import Transaction


def create_transaction(
    db: Session,
    user_id: int,
    transaction_data: TransactionCreate
):
    transaction = Transaction(
        user_id=user_id,
        category_id=transaction_data.category_id,
        amount=transaction_data.amount,
        description=transaction_data.description,
        transaction_type=transaction_data.transaction_type,
        transaction_date=transaction_data.transaction_date,
        transaction_time=transaction_data.transaction_time
    )

    db.add(transaction)
    db.commit()
    db.refresh(transaction)

    return transaction


def get_user_transaction(
    db: Session,
    user_id: int
):
    return (
        db.query(Transaction)
        .filter(Transaction.user_id == user_id)
        .order_by(
            Transaction.transaction_date.desc(),
            Transaction.transaction_time.desc(),
            Transaction.created_at.desc()
        )
        .all()
    )


def get_transaction(
    db: Session,
    user_id: int,
    transaction_id: int
):
    return (
        db.query(Transaction)
        .filter(
            Transaction.id == transaction_id,
            Transaction.user_id == user_id
        )
        .first()
    )


def update_transaction(
    db: Session,
    transaction: Transaction,
    transaction_data: TransactionCreate
):
    transaction.category_id = transaction_data.category_id
    transaction.amount = transaction_data.amount
    transaction.description = transaction_data.description
    transaction.transaction_type = transaction_data.transaction_type
    transaction.transaction_date = transaction_data.transaction_date
    transaction.transaction_time = transaction_data.transaction_time

    db.commit()
    db.refresh(transaction)

    return transaction


def delete_transaction(
    db: Session,
    transaction: Transaction
):
    db.delete(transaction)
    db.commit()