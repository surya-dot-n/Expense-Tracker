from pydantic import BaseModel

class TokenResponse(BaseModel):
    access_token:str
    token_type:str

class GoogleUserResponse(BaseModel):
    name: str
    email: str
    google_id:str

