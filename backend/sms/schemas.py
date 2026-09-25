from datetime import datetime

from pydantic import BaseModel


class SMSCreate(BaseModel):
    sender: str
    message: str
    received_at: datetime


class SMSResponse(BaseModel):
    id: int
    user_id: int
    sender: str
    message: str
    received_at: datetime
    created_at: datetime

    class Config:
        from_attributes = True


class TransactionExtracted(BaseModel):
    amount: float | None = None
    transaction_type: str | None = None
    merchant: str | None = None
    description: str | None = None