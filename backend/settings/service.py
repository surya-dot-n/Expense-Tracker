from sqlalchemy.orm import Session

from models.settings import UserSettings
from models.transaction_source import TransactionSource
import json


ALLOWED_DETECTION_MODES = {
    "AUTO",
    "APPROVAL",
    "MANUAL"
}

DEFAULT_SMS_SOURCE_NAME = "SMS (Default)"


def ensure_default_sms_source(db: Session, user_id: int):
    """Ensure every user has one built-in SMS source.

    The default SMS source deliberately has an empty sender list. Android
    treats an empty sender list on this built-in source as "all SMS senders"
    and the transaction parser decides whether the message looks like a
    financial transaction.
    """
    source = (
        db.query(TransactionSource)
        .filter(
            TransactionSource.user_id == user_id,
            TransactionSource.source_type == "SMS",
            TransactionSource.app_name == DEFAULT_SMS_SOURCE_NAME
        )
        .first()
    )

    if source:
        return source

    source = TransactionSource(
        user_id=user_id,
        app_name=DEFAULT_SMS_SOURCE_NAME,
        package_name=None,
        source_type="SMS",
        sender_ids=json.dumps([]),
        enabled=True
    )

    db.add(source)
    db.commit()
    db.refresh(source)
    return source


def get_or_create_settings(db: Session, user_id: int):
    settings = (
        db.query(UserSettings)
        .filter(UserSettings.user_id == user_id)
        .first()
    )

    if not settings:
        settings = UserSettings(
            user_id=user_id,
            notifications_enabled=True,
            sms_detection_enabled=True,
            detection_mode="APPROVAL"
        )
        db.add(settings)
        db.commit()
        db.refresh(settings)
    else:
        # Existing installations created with the old default had SMS
        # detection disabled. Do not force-enable a user's explicit choice;
        # only the newly-created settings use the new default.
        pass

    ensure_default_sms_source(db, user_id)
    return settings


def update_settings(
    db: Session,
    user_id: int,
    notifications_enabled=None,
    sms_detection_enabled=None,
    detection_mode=None
):
    settings = get_or_create_settings(db, user_id)

    if notifications_enabled is not None:
        settings.notifications_enabled = notifications_enabled

    if sms_detection_enabled is not None:
        settings.sms_detection_enabled = sms_detection_enabled

    if detection_mode is not None:
        detection_mode = detection_mode.upper()
        if detection_mode not in ALLOWED_DETECTION_MODES:
            raise ValueError(
                "Invalid detection mode. Use AUTO, APPROVAL, or MANUAL."
            )
        settings.detection_mode = detection_mode

    db.commit()
    db.refresh(settings)
    return settings
