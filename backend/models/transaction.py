from sqlalchemy import Column, Integer, String, ForeignKey, Numeric, DateTime, Date, Time
from database.base import Base
from sqlalchemy.sql import func


class Transaction(Base):
    __tablename__ = "transactions"

    id = Column(Integer, primary_key=True, index=True)

    user_id = Column(
        Integer,
        ForeignKey("users.id"),
        nullable=False
    )

    category_id = Column(
        Integer,
        ForeignKey("categories.id"),
        nullable=False
    )

    amount = Column(
        Numeric(12, 2),
        nullable=False
    )

    description = Column(
        String,
        nullable=True
    )

    transaction_type = Column(
        String,
        nullable=False
    )

    # Actual date of the transaction
    transaction_date = Column(
        Date,
        nullable=False
    )

    # Optional actual time of the transaction
    transaction_time = Column(
        Time,
        nullable=True
    )

    # When TREX created the database record
    created_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False
    )

    # When the database record was last modified
    updated_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False
    )