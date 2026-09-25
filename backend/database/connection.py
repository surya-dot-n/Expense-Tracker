from sqlalchemy import create_engine
from starlette.config import Config

config = Config(".env")

engine = create_engine(config("DATABASE_URL"))
