import json

from sqlalchemy.orm import Session

from models.transaction_source import TransactionSource
from settings.service import ensure_default_sms_source


def get_sources(db: Session, user_id: int):

    ensure_default_sms_source(
        db,
        user_id
    )

    return (
        db.query(TransactionSource)
        .filter(
            TransactionSource.user_id == user_id
        )
        .order_by(
            TransactionSource.app_name
        )
        .all()
    )


def get_source(
    db: Session,
    user_id: int,
    source_id: int
):

    return (
        db.query(TransactionSource)
        .filter(
            TransactionSource.id == source_id,
            TransactionSource.user_id == user_id
        )
        .first()
    )


def create_source(
    db: Session,
    user_id: int,
    app_name: str,
    package_name: str | None,
    source_type: str,
    sender_ids: list[str] | None,
    enabled: bool
):

    # --------------------------------------------------------
    # Prevent duplicate APP package registrations.
    # --------------------------------------------------------

    if package_name:

        existing_package = (
            db.query(TransactionSource)
            .filter(
                TransactionSource.user_id == user_id,
                TransactionSource.package_name == package_name
            )
            .first()
        )

        if existing_package:

            raise ValueError(
                "This app is already added as a transaction source."
            )

    # --------------------------------------------------------
    # For SMS-only sources, prevent duplicate identical SMS
    # source configurations.
    # --------------------------------------------------------

    if source_type == "SMS":

        existing_sms = (
            db.query(TransactionSource)
            .filter(
                TransactionSource.user_id == user_id,
                TransactionSource.app_name == app_name,
                TransactionSource.source_type == "SMS"
            )
            .first()
        )

        if existing_sms:

            raise ValueError(
                "This SMS transaction source already exists."
            )

    source = TransactionSource(
        user_id=user_id,
        app_name=app_name,
        package_name=package_name,
        source_type=source_type,
        sender_ids=json.dumps(
            sender_ids or []
        ),
        enabled=enabled
    )

    db.add(source)
    db.commit()
    db.refresh(source)

    return source


def update_source(
    db: Session,
    user_id: int,
    source_id: int,
    app_name: str | None = None,
    package_name: str | None = None,
    source_type: str | None = None,
    sender_ids: list[str] | None = None,
    enabled: bool | None = None
):

    source = get_source(
        db,
        user_id,
        source_id
    )

    if not source:

        raise ValueError(
            "Transaction source not found."
        )

    # --------------------------------------------------------
    # Prevent assigning an app package that already belongs
    # to another source.
    # --------------------------------------------------------

    if (
        package_name is not None
        and package_name != source.package_name
    ):

        existing_package = (
            db.query(TransactionSource)
            .filter(
                TransactionSource.user_id == user_id,
                TransactionSource.package_name == package_name,
                TransactionSource.id != source_id
            )
            .first()
        )

        if existing_package:

            raise ValueError(
                "This app is already added as a transaction source."
            )

    if app_name is not None:
        source.app_name = app_name

    if package_name is not None:
        source.package_name = package_name

    if source_type is not None:
        source.source_type = source_type

    if sender_ids is not None:
        source.sender_ids = json.dumps(
            sender_ids
        )

    if enabled is not None:
        source.enabled = enabled

    db.commit()
    db.refresh(source)

    return source


def delete_source(
    db: Session,
    user_id: int,
    source_id: int
):

    source = get_source(
        db,
        user_id,
        source_id
    )

    if not source:

        raise ValueError(
            "Transaction source not found."
        )

    if (
        source.source_type == "SMS"
        and source.app_name == "SMS (Default)"
    ):

        raise ValueError(
            "The default SMS source cannot be deleted. "
            "Disable SMS detection or disable this source instead."
        )

    db.delete(source)
    db.commit()