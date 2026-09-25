from sqlalchemy import Column, Integer, String, ForeignKey
from database.base import Base


class Category(Base):
    __tablename__ = "categories"

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

    category_name = Column(
        String(100),
        nullable=False
    )

    category_type = Column(
        String(50),
        nullable=False
    )