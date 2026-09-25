import jwt
from datetime import datetime,timedelta
from starlette.config import Config
config = Config(".env")

ALGORITHM = "HS256"
jwt_sec = config("JWT_SEC")

def create_access_token(data : dict):
    payload = data.copy()
    expire = datetime.utcnow() + timedelta(days = 7)
    payload["exp"] = expire
    token = jwt.encode(
        payload,
        jwt_sec,
        algorithm = ALGORITHM
    )

    return token

def decode_access_token(token : str):
    payload = jwt.decode(
        token,
        jwt_sec,
        algorithms=[ALGORITHM]
    )

    return payload
