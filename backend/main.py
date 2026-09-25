from fastapi import FastAPI

from starlette.middleware.sessions import SessionMiddleware

from database.connection import engine
from database.base import Base

from auth.router import router as auth_router
from categories.router import router as categories_router
from transaction.router import router as trans_router
from dashboard.router import router as dashboard_router
from sms.router import router as sms_router
from users.router import router as users_router
from settings.router import router as settings_router
from pending_transaction.router import router as pending_transaction_router


# ============================================================
# MODEL IMPORTS
# ============================================================

from models.settings import UserSettings
from models.transaction_source import TransactionSource
from models.pending_transaction import PendingTransaction


# ============================================================
# APP
# ============================================================

app = FastAPI()


# ============================================================
# SESSION
# ============================================================

app.add_middleware(
    SessionMiddleware,
    secret_key="123@56"
)


# ============================================================
# DATABASE TABLE CREATION
# ============================================================

Base.metadata.create_all(
    bind=engine
)


# ============================================================
# ROUTERS
# ============================================================

app.include_router(
    auth_router
)

app.include_router(
    categories_router
)

app.include_router(
    trans_router
)

app.include_router(
    dashboard_router
)

app.include_router(
    sms_router,
    prefix="/sms",
    tags=["SMS"]
)
app.include_router(
    users_router
)

app.include_router(
    settings_router
)

app.include_router(
    pending_transaction_router
)