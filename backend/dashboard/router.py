from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from database.session import get_db
from .schemas import (
    DashboardSummary,
    CategorySummary,
    RecentTransaction
)
from .service import (
    get_category_summary,
    get_dashboard_summary,
    get_recent_transactions
)
from auth.dependencies import get_current_user

router = APIRouter(
    prefix="/dashboard",
    tags=["Dashboard"]
)

@router.get(
    "/summary",
    response_model=DashboardSummary
)
def dashboard_summary(
    db: Session = Depends(get_db),
    current_user = Depends(get_current_user)
):
    return get_dashboard_summary(
        db,
        current_user.id
    )

@router.get(
    "/categories",
    response_model=list[CategorySummary]
)
def category_summary(
    db: Session = Depends(get_db),
    current_user = Depends(get_current_user)
):
    return get_category_summary(
        db,
        current_user.id
    )

@router.get(
    "/recent",
    response_model=list[RecentTransaction]
)
def recent_transaction(
    db: Session = Depends(get_db),
    current_user = Depends(get_current_user)
):
    return get_recent_transactions(
        db,
        current_user.id
    )