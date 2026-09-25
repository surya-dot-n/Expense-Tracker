from sqlalchemy import (
    Column,
    Integer,
    String,
    Boolean,
    ForeignKey,
    DateTime,
    Text
)

from sqlalchemy.sql import func

from database.base import Base


class TransactionSource(Base):

    __tablename__ = "transaction_sources"

    id = Column(
        Integer,
        primary_key=True,
        index=True
    )

    user_id = Column(
        Integer,
        ForeignKey("users.id"),
        nullable=False
    )

    app_name = Column(
        String,
        nullable=False
    )

    package_name = Column(
        String,
        nullable=True
    )

    source_type = Column(
        String,
        nullable=False
    )

    sender_ids = Column(
        Text,
        nullable=True
    )

    enabled = Column(
        Boolean,
        default=True,
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