from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from database.session import get_db

from .schemas import (
    SMSCreate,
    SMSResponse,
    TransactionExtracted
)

from .service import (
    save_sms,
    get_all_sms,
    get_sms_by_id,
    extract_transaction,
    create_transaction_from_sms
)


router = APIRouter()


@router.post(
    "/",
    response_model=SMSResponse
)
def create_sms(
    sms_data: SMSCreate,
    user_id: int,
    db: Session = Depends(get_db)
):

    return save_sms(
        db=db,
        sms_data=sms_data,
        user_id=user_id
    )


@router.get(
    "/",
    response_model=list[SMSResponse]
)
def read_all_sms(
    user_id: int,
    db: Session = Depends(get_db)
):

    return get_all_sms(
        db=db,
        user_id=user_id
    )


@router.get(
    "/{sms_id}",
    response_model=SMSResponse
)
def read_sms(
    sms_id: int,
    user_id: int,
    db: Session = Depends(get_db)
):

    sms = get_sms_by_id(
        db=db,
        sms_id=sms_id,
        user_id=user_id
    )

    if not sms:

        raise HTTPException(
            status_code=404,
            detail="SMS not found"
        )

    return sms


@router.post(
    "/{sms_id}/extract",
    response_model=TransactionExtracted
)
def extract_sms_transaction(
    sms_id: int,
    user_id: int,
    db: Session = Depends(get_db)
):

    sms = get_sms_by_id(
        db=db,
        sms_id=sms_id,
        user_id=user_id
    )

    if not sms:

        raise HTTPException(
            status_code=404,
            detail="SMS not found"
        )

    return extract_transaction(
        sms.message
    )

@router.post("/{sms_id}/create-transaction")
def create_transaction(
    sms_id: int,
    user_id: int,
    db: Session = Depends(get_db)
):
    transaction, error = create_transaction_from_sms(
        db=db,
        sms_id=sms_id,
        user_id=user_id
    )

    if error:
        raise HTTPException(
            status_code=400,
            detail=error
        )

    return transaction

