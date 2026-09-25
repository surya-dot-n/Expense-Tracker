from fastapi import (
    APIRouter,
    Depends,
    HTTPException
)

from sqlalchemy.orm import Session

from database.session import get_db

from auth.dependencies import get_current_user

from pending_transaction.schemas import (
    PendingTransactionCreate,
    PendingTransactionResponse
)

from pending_transaction.service import (
    get_pending_transactions,
    create_pending_transaction,
    get_pending_transaction,
    update_pending_transaction_category,
    approve_pending_transaction,
    deny_pending_transaction,
    delete_pending_transaction
)


router = APIRouter(
    prefix="/pending-transactions",
    tags=["Pending Transactions"]
)


# ============================================================
# GET ALL PENDING TRANSACTIONS
# ============================================================

@router.get(
    "",
    response_model=list[PendingTransactionResponse]
)
def get_pending(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):

    return get_pending_transactions(
        db=db,
        user_id=current_user.id
    )


# ============================================================
# CREATE PENDING TRANSACTION
# ============================================================

@router.post(
    "",
    response_model=PendingTransactionResponse
)
def create_pending(
    data: PendingTransactionCreate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):

    source_type = data.source_type.upper()

    allowed_source_types = {
        "SMS",
        "APP_NOTIFICATION",
        "BOTH"
    }

    if source_type not in allowed_source_types:

        raise HTTPException(
            status_code=400,
            detail=(
                "Invalid source type. "
                "Use SMS, APP_NOTIFICATION, or BOTH."
            )
        )

    if data.amount <= 0:

        raise HTTPException(
            status_code=400,
            detail="Amount must be greater than zero."
        )

    transaction_type = data.transaction_type.capitalize()

    if transaction_type not in {
        "Expense",
        "Income"
    }:

        raise HTTPException(
            status_code=400,
            detail=(
                "Invalid transaction type. "
                "Use Expense or Income."
            )
        )

    pending = create_pending_transaction(

        db=db,

        user_id=current_user.id,

        source_id=data.source_id,

        app_name=data.app_name,

        package_name=data.package_name,

        source_type=source_type,

        sender_id=data.sender_id,

        message_body=data.message_body,

        amount=data.amount,

        transaction_type=transaction_type,

        description=data.description,

        category_id=data.category_id,

        transaction_date=data.transaction_date,

        confidence=data.confidence,

        fingerprint=data.fingerprint
    )

    return pending


# ============================================================
# UPDATE CATEGORY
# ============================================================

@router.patch(
    "/{pending_id}/category",
    response_model=PendingTransactionResponse
)
def update_pending_category(
    pending_id: int,
    category_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):

    try:

        pending = update_pending_transaction_category(

            db=db,

            user_id=current_user.id,

            pending_id=pending_id,

            category_id=category_id
        )

        return pending

    except ValueError as e:

        raise HTTPException(
            status_code=404,
            detail=str(e)
        )


# ============================================================
# APPROVE
# ============================================================

@router.post(
    "/{pending_id}/approve"
)
def approve_pending(
    pending_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):

    try:

        transaction, pending = (
            approve_pending_transaction(

                db=db,

                user_id=current_user.id,

                pending_id=pending_id
            )
        )

        return {
            "message": (
                "Pending transaction "
                "approved successfully."
            ),
            "pending_transaction_id": pending.id,
            "transaction_id": transaction.id,
            "status": pending.status
        }

    except ValueError as e:

        raise HTTPException(
            status_code=400,
            detail=str(e)
        )


# ============================================================
# DENY
# ============================================================

@router.post(
    "/{pending_id}/deny"
)
def deny_pending(
    pending_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):

    try:

        pending = deny_pending_transaction(

            db=db,

            user_id=current_user.id,

            pending_id=pending_id
        )

        return {
            "message": (
                "Pending transaction "
                "denied successfully."
            ),
            "pending_transaction_id": pending.id,
            "status": pending.status
        }

    except ValueError as e:

        raise HTTPException(
            status_code=404,
            detail=str(e)
        )


# ============================================================
# DELETE
# ============================================================

@router.delete(
    "/{pending_id}"
)
def delete_pending(
    pending_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):

    try:

        delete_pending_transaction(

            db=db,

            user_id=current_user.id,

            pending_id=pending_id
        )

        return {
            "message": (
                "Pending transaction "
                "deleted successfully."
            )
        }

    except ValueError as e:

        raise HTTPException(
            status_code=404,
            detail=str(e)
        )