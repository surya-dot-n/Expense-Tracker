from sqlalchemy import (
    Column,
    Integer,
    Boolean,
    String,
    ForeignKey,
    DateTime
)

from sqlalchemy.sql import func

from database.base import Base


class UserSettings(Base):

    __tablename__ = "user_settings"

    id = Column(
        Integer,
        primary_key=True,
        index=True
    )

    user_id = Column(
        Integer,
        ForeignKey("users.id"),
        unique=True,
        nullable=False
    )

    notifications_enabled = Column(
        Boolean,
        default=True,
        nullable=False
    )

    sms_detection_enabled = Column(
        Boolean,
        default=False,
        nullable=False
    )

    detection_mode = Column(
        String,
        default="APPROVAL",
        nullable=False
    )

    created_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False
    )

    updated_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False
    )