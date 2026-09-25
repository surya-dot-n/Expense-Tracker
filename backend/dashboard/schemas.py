from pydantic import BaseModel
from decimal import Decimal


class DashboardSummary(BaseModel):

    total_income: Decimal
    total_expense: Decimal
    balance: Decimal


class CategorySummary(BaseModel):

    category_id: int
    category_name: str
    total_amount: Decimal


class RecentTransaction(BaseModel):

    id: int
    category_id: int
    amount: Decimal
    description: str | None = None
    transaction_type: str


class DashboardResponse(BaseModel):

    summary: DashboardSummary
    category_summary: list[CategorySummary]
    recent_transaction: list[RecentTransaction]