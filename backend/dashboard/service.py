from sqlalchemy.orm import Session
from sqlalchemy import func

from models.transaction import Transaction
from models.category import Category

def get_dashboard_summary(
        db: Session,
        user_id: int
):
    total_income = (
        db.query(func.coalesce(func.sum(Transaction.amount),0))
        .filter(
            Transaction.user_id == user_id,
            Transaction.transaction_type == "income"
        )
        .scalar()
    )

    total_expense = (
        db.query(func.coalesce(func.sum(Transaction.amount),0))
        .filter(
            Transaction.user_id == user_id,
            Transaction.transaction_type == "expense"
        ).scalar()
    )

    balance = total_income - total_expense

    return {
        "total_income" : total_income,
        "total_expense" : total_expense,
        "balance" : balance
    }

def get_category_summary(
        db: Session,
        user_id: int
):
    results = (
        db.query(
            Category.id,
            Category.category_name,
            func.sum(Transaction.amount).label("total_amount")
        )
        .join(
            Transaction,
            Transaction.category_id == Category.id
        )
        .filter(
            Transaction.user_id == user_id,
            Transaction.transaction_type == "expense"
        )
        .group_by(
            Category.id,
            Category.category_name
        )
        .all()
    )

    return [
        {
            "category_id": category_id,
            "category_name": category_name,
            "total_amount": total_amount
        }
        for category_id,category_name,total_amount in results
    ]

def get_recent_transactions(
        db: Session,
        user_id: int,
        limit: int = 5
):
    return (
        db.query(Transaction)
        .filter(Transaction.user_id == user_id)
        .order_by(Transaction.created_at.desc())
        .limit(limit)
        .all()
    )
