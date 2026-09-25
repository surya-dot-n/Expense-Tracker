from sqlalchemy.orm import Session

from models.category import Category
from categories.schemas import CategoryCreate

def create_category(
        db: Session,
        user_id: int,
        category_data: CategoryCreate
):
    category = Category(
        user_id = user_id,
        category_name = category_data.category_name,
        category_type = category_data.category_type
    )

    db.add(category)
    db.commit()
    db.refresh(category)

    return category

def get_user_categories(
        db: Session,
        user_id: int
):
    return (
        db.query(Category)
        .filter(Category.user_id == user_id)
        .all()
    )

def get_category_by_id(
        db: Session,
        user_id: int,
        category_id: int
):
    return (
        db.query(Category)
        .filter(Category.id == category_id,Category.user_id == user_id).first()
    )


def update_category(
        db: Session,
        user_id: int,
        category_id: int,
        category_data: CategoryCreate
):
    category = (
        db.query(Category)
        .filter(
            Category.id == category_id,
            Category.user_id == user_id
        ).first()
    )

    if not category:
        return None

    category.category_name = category_data.category_name
    category.category_type = category_data.category_type

    db.commit()
    db.refresh(category)

    return category

def delete_category(
        db: Session,
        user_id: int,
        category_id: int
):
    category = (
        db.query(Category)
        .filter(
            Category.id == category_id,
            Category.user_id == user_id
        ).first()
    )

    if not category:
        return None

    db.delete(category)
    db.commit()

    return category



    