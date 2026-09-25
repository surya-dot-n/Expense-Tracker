import json

from fastapi import (
    APIRouter,
    Depends,
    HTTPException
)

from sqlalchemy.orm import Session

from database.session import get_db

from settings.schemas import (
    SettingsResponse,
    SettingsUpdate,
    TransactionSourceCreate,
    TransactionSourceUpdate,
    TransactionSourceResponse
)

from settings.service import (
    get_or_create_settings,
    update_settings
)

from settings.sources import (
    get_sources,
    create_source,
    update_source,
    delete_source
)

from auth.dependencies import get_current_user


router = APIRouter(
    prefix="/settings",
    tags=["Settings"]
)


# ============================================================
# SETTINGS
# ============================================================


@router.get(
    "",
    response_model=SettingsResponse
)
def get_settings(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):

    return get_or_create_settings(
        db=db,
        user_id=current_user.id
    )


@router.put(
    "",
    response_model=SettingsResponse
)
def update_user_settings(
    data: SettingsUpdate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):

    try:

        return update_settings(
            db=db,
            user_id=current_user.id,
            notifications_enabled=data.notifications_enabled,
            sms_detection_enabled=data.sms_detection_enabled,
            detection_mode=data.detection_mode
        )

    except ValueError as e:

        raise HTTPException(
            status_code=400,
            detail=str(e)
        )


# ============================================================
# TRANSACTION SOURCES
# ============================================================


@router.get(
    "/sources",
    response_model=list[TransactionSourceResponse]
)
def get_transaction_sources(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):

    sources = get_sources(
        db=db,
        user_id=current_user.id
    )

    result = []

    for source in sources:

        try:
            sender_ids = json.loads(
                source.sender_ids or "[]"
            )
        except (json.JSONDecodeError, TypeError):
            sender_ids = []

        result.append(
            TransactionSourceResponse(
                id=source.id,
                app_name=source.app_name,
                package_name=source.package_name,
                source_type=source.source_type,
                sender_ids=sender_ids,
                enabled=source.enabled
            )
        )

    return result


# ============================================================
# CREATE SOURCE
# ============================================================


@router.post(
    "/sources",
    response_model=TransactionSourceResponse
)
def add_transaction_source(
    data: TransactionSourceCreate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):

    allowed_types = {
        "SMS",
        "APP_NOTIFICATION",
        "BOTH"
    }

    source_type = data.source_type.upper().strip()

    if source_type not in allowed_types:

        raise HTTPException(
            status_code=400,
            detail=(
                "Invalid source type. "
                "Use SMS, APP_NOTIFICATION, or BOTH."
            )
        )

    # --------------------------------------------------------
    # APP NOTIFICATION
    # --------------------------------------------------------

    if source_type in {
        "APP_NOTIFICATION",
        "BOTH"
    }:

        if not data.package_name or not data.package_name.strip():

            raise HTTPException(
                status_code=400,
                detail=(
                    "package_name is required "
                    "for app notification sources."
                )
            )

    # --------------------------------------------------------
    # SMS
    # --------------------------------------------------------
    #
    # sender_ids are now OPTIONAL.
    #
    # This allows the user to create an SMS source first
    # and configure sender IDs later.
    # --------------------------------------------------------

    sender_ids = data.sender_ids or []

    # Clean sender IDs
    sender_ids = [
        sender.strip()
        for sender in sender_ids
        if sender and sender.strip()
    ]

    package_name = (
        data.package_name.strip()
        if data.package_name
        else None
    )

    app_name = data.app_name.strip()

    if not app_name:

        raise HTTPException(
            status_code=400,
            detail="app_name cannot be empty."
        )

    try:

        source = create_source(
            db=db,
            user_id=current_user.id,
            app_name=app_name,
            package_name=package_name,
            source_type=source_type,
            sender_ids=sender_ids,
            enabled=data.enabled
        )

        try:
            response_sender_ids = json.loads(
                source.sender_ids or "[]"
            )
        except (json.JSONDecodeError, TypeError):
            response_sender_ids = []

        return TransactionSourceResponse(
            id=source.id,
            app_name=source.app_name,
            package_name=source.package_name,
            source_type=source.source_type,
            sender_ids=response_sender_ids,
            enabled=source.enabled
        )

    except ValueError as e:

        raise HTTPException(
            status_code=400,
            detail=str(e)
        )


# ============================================================
# UPDATE SOURCE
# ============================================================


@router.put(
    "/sources/{source_id}",
    response_model=TransactionSourceResponse
)
def edit_transaction_source(
    source_id: int,
    data: TransactionSourceUpdate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):

    source_type = None

    if data.source_type is not None:

        source_type = data.source_type.upper().strip()

        allowed_types = {
            "SMS",
            "APP_NOTIFICATION",
            "BOTH"
        }

        if source_type not in allowed_types:

            raise HTTPException(
                status_code=400,
                detail=(
                    "Invalid source type. "
                    "Use SMS, APP_NOTIFICATION, or BOTH."
                )
            )

        if source_type in {
            "APP_NOTIFICATION",
            "BOTH"
        }:

            # If package_name isn't supplied during update,
            # retain the existing package name.
            if (
                data.package_name is not None
                and not data.package_name.strip()
            ):

                raise HTTPException(
                    status_code=400,
                    detail=(
                        "package_name cannot be empty "
                        "for app notification sources."
                    )
                )

    sender_ids = None

    if data.sender_ids is not None:

        sender_ids = [
            sender.strip()
            for sender in data.sender_ids
            if sender and sender.strip()
        ]

    package_name = None

    if data.package_name is not None:

        package_name = data.package_name.strip()

    app_name = None

    if data.app_name is not None:

        app_name = data.app_name.strip()

        if not app_name:

            raise HTTPException(
                status_code=400,
                detail="app_name cannot be empty."
            )

    try:

        source = update_source(
            db=db,
            user_id=current_user.id,
            source_id=source_id,
            app_name=app_name,
            package_name=package_name,
            source_type=source_type,
            sender_ids=sender_ids,
            enabled=data.enabled
        )

        try:
            response_sender_ids = json.loads(
                source.sender_ids or "[]"
            )
        except (json.JSONDecodeError, TypeError):
            response_sender_ids = []

        return TransactionSourceResponse(
            id=source.id,
            app_name=source.app_name,
            package_name=source.package_name,
            source_type=source.source_type,
            sender_ids=response_sender_ids,
            enabled=source.enabled
        )

    except ValueError as e:

        raise HTTPException(
            status_code=404,
            detail=str(e)
        )


# ============================================================
# DELETE SOURCE
# ============================================================


@router.delete(
    "/sources/{source_id}"
)
def remove_transaction_source(
    source_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):

    try:

        delete_source(
            db=db,
            user_id=current_user.id,
            source_id=source_id
        )

        return {
            "message": (
                "Transaction source "
                "deleted successfully."
            )
        }

    except ValueError as e:

        raise HTTPException(
            status_code=400,
            detail=str(e)
        )