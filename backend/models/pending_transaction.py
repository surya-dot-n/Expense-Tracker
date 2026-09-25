from sqlalchemy import (
    Column,
    Integer,
    String,
    Numeric,
    ForeignKey,
    DateTime,
    Text
)

from sqlalchemy.sql import func

from database.base import Base


class PendingTransaction(Base):

    __tablename__ = "pending_transactions"

    id = Column(
        Integer,
        primary_key=True,
        index=True
    )

    user_id = Column(
        Integer,
        ForeignKey("users.id"),
        nullable=False,
        index=True
    )

    source_id = Column(
        Integer,
        ForeignKey("transaction_sources.id"),
        nullable=True,
        index=True
    )

    # --------------------------------------------------------
    # Source information
    # --------------------------------------------------------

    app_name = Column(
        String,
        nullable=True
    )

    package_name = Column(
        String,
        nullable=True
    )

    source_type = Column(
        String,
        nullable=False
    )

    sender_id = Column(
        String,
        nullable=True
    )

    # --------------------------------------------------------
    # Original message
    # --------------------------------------------------------

    message_body = Column(
        Text,
        nullable=True
    )

    # --------------------------------------------------------
    # Detected transaction information
    # --------------------------------------------------------

    amount = Column(
        Numeric(12, 2),
        nullable=False
    )

    transaction_type = Column(
        String,
        nullable=False
    )

    description = Column(
        String,
        nullable=True
    )

    category_id = Column(
        Integer,
        ForeignKey("categories.id"),
        nullable=True
    )

    # --------------------------------------------------------
    # Transaction date/time detected from message
    # --------------------------------------------------------

    transaction_date = Column(
        DateTime(timezone=True),
        nullable=True
    )

    # --------------------------------------------------------
    # Detection information
    # --------------------------------------------------------

    confidence = Column(
        Numeric(5, 2),
        nullable=True
    )

    fingerprint = Column(
        String,
        nullable=True,
        index=True
    )

    # --------------------------------------------------------
    # Pending status
    # --------------------------------------------------------

    status = Column(
        String,
        default="PENDING",
        nullable=False,
        index=True
    )

    # --------------------------------------------------------
    # Timestamps
    # --------------------------------------------------------

    detected_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
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