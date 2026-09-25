from datetime import datetime

from pydantic import BaseModel, ConfigDict


class SettingsResponse(BaseModel):

    notifications_enabled: bool

    sms_detection_enabled: bool

    detection_mode: str

    created_at: datetime

    updated_at: datetime

    model_config = ConfigDict(
        from_attributes=True
    )


class SettingsUpdate(BaseModel):

    notifications_enabled: bool | None = None

    sms_detection_enabled: bool | None = None

    detection_mode: str | None = None


class TransactionSourceCreate(BaseModel):

    app_name: str

    package_name: str | None = None

    source_type: str

    sender_ids: list[str] | None = None

    enabled: bool = True


class TransactionSourceUpdate(BaseModel):

    app_name: str | None = None

    package_name: str | None = None

    source_type: str | None = None

    sender_ids: list[str] | None = None

    enabled: bool | None = None


class TransactionSourceResponse(BaseModel):

    id: int

    app_name: str

    package_name: str | None

    source_type: str

    sender_ids: list[str]

    enabled: bool

    model_config = ConfigDict(
        from_attributes=True
    )