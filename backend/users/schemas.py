from pydantic import BaseModel
from datetime import datetime

class UserResponse(BaseModel):
    id: int
    name: str
    email: str
    google_id: str
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attribute = True

class UserUpdate(BaseModel):
    name: str