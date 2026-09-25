from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from database.session import get_db
from .schemas import TransactionCreate, TransactionResponse

from .service import (
    create_transaction,
    get_user_transaction,
    get_transaction,
    update_transaction,
    delete_transaction
)

from auth.dependencies import get_current_user


router = APIRouter(
    prefix="/transactions",
    tags=["Transactions"]
)


@router.post(
    "/",
    response_model=TransactionResponse,
    status_code=status.HTTP_201_CREATED
)
def add_transaction(
    transaction_data: TransactionCreate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):
    return create_transaction(
        db,
        current_user.id,
        transaction_data
    )


@router.get(
    "/",
    response_model=list[TransactionResponse]
)
def get_transactions(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):
    return get_user_transaction(
        db,
        current_user.id
    )


@router.get(
    "/{transaction_id}",
    response_model=TransactionResponse
)
def get_single_transaction(
    transaction_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):
    transaction = get_transaction(
        db,
        current_user.id,
        transaction_id
    )

    if not transaction:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Transaction not found"
        )

    return transaction


@router.put(
    "/{transaction_id}",
    response_model=TransactionResponse
)
def edit_transaction(
    transaction_id: int,
    transaction_data: TransactionCreate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):
    transaction = get_transaction(
        db,
        current_user.id,
        transaction_id
    )

    if not transaction:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Transaction not found"
        )

    return update_transaction(
        db,
        transaction,
        transaction_data
    )


@router.delete(
    "/{transaction_id}",
    status_code=status.HTTP_204_NO_CONTENT
)
def remove_transaction(
    transaction_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):
    transaction = get_transaction(
        db,
        current_user.id,
        transaction_id
    )

    if not transaction:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Transaction not found"
        )

    delete_transaction(
        db,
        transaction
    )

    return None