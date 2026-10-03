from sqlalchemy import create_engine
from starlette.config import Config

config = Config(".env")

DATABASE_URL = config("DATABASE_URL")

engine = create_engine(
    DATABASE_URL,
    pool_pre_ping=True,
    pool_recycle=1800,
)
