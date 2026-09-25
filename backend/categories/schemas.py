from pydantic import BaseModel, Field
from typing import Optional

class CategoryCreate(BaseModel):
    category_name : str = Field (
        ...,
        min_length= 1,
        max_length= 100
    )

    category_type : str = Field(
        ...,
        min_length=1,
        max_length=50
    )

class CategoryResponse(BaseModel):
    id: int
    category_name: str
    category_type: str

    class Config:
        from_attributes = True

