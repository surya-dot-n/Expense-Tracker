from pydantic import BaseModel, ConfigDict, Field
from datetime import date, time, datetime
from decimal import Decimal

class TransactionCreate(BaseModel):
    category_id: int

    amount: Decimal = Field(
        gt=0
    )

    description: str | None = None

    transaction_type: str

    # If Android does not send a date,
    # TREX automatically uses today's date.
    transaction_date: date = Field(
        default_factory=date.today
    )

    transaction_time: time | None = None

class TransactionUpdate(BaseModel):
    category_id: int | None = None

    amount: Decimal | None = Field(
        default=None,
        gt=0
    )

    description: str | None = None

    transaction_type: str | None = None

    transaction_date: date | None = None

    transaction_time: time | None = None

class TransactionResponse(BaseModel):
    id: int

    user_id: int

    category_id: int

    amount: Decimal

    description: str | None

    transaction_type: str

    transaction_date: date

    transaction_time: time | None

    created_at: datetime

    updated_at: datetime

    model_config = ConfigDict(
        from_attributes=True
    )

