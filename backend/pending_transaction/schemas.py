from datetime import datetime
from decimal import Decimal

from pydantic import BaseModel, ConfigDict


class PendingTransactionCreate(BaseModel):

    source_id: int | None = None

    app_name: str | None = None

    package_name: str | None = None

    source_type: str

    sender_id: str | None = None

    message_body: str | None = None

    amount: Decimal

    transaction_type: str

    description: str | None = None

    category_id: int | None = None

    transaction_date: datetime | None = None

    confidence: Decimal | None = None

    fingerprint: str | None = None


class PendingTransactionResponse(BaseModel):

    id: int

    source_id: int | None

    app_name: str | None

    package_name: str | None

    source_type: str

    sender_id: str | None

    message_body: str | None

    amount: Decimal

    transaction_type: str

    description: str | None

    category_id: int | None

    transaction_date: datetime | None

    confidence: Decimal | None

    fingerprint: str | None

    status: str

    detected_at: datetime

    created_at: datetime

    updated_at: datetime

    model_config = ConfigDict(
        from_attributes=True
    )