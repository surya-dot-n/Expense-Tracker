from fastapi import APIRouter, Request, Depends
from sqlalchemy.orm import Session
from fastapi.responses import RedirectResponse

from .oauth import oauth
from .jwt import create_access_token

from database.session import get_db
from users.service import get_or_create_user

from .dependencies import get_current_user


router = APIRouter(
    prefix="/auth",
    tags=["Authentication"]
)


@router.get("/google")
async def google_login(request: Request):

    redirect_uri = request.url_for("google_callback")

    return await oauth.google.authorize_redirect(
        request,
        redirect_uri,
        prompt="select_account"
    )


@router.get(
    "/google/callback",
    name="google_callback"
)
async def google_callback(
    request: Request,
    db: Session = Depends(get_db)
):

    token = await oauth.google.authorize_access_token(request)

    user_info = token.get("userinfo")

    email = user_info["email"]
    name = user_info["name"]
    google_id = user_info["sub"]

    user = get_or_create_user(
        db=db,
        name=name,
        email=email,
        google_id=google_id
    )

    access_token = create_access_token({
        "user_id": user.id,
        "email": user.email
    })

    return RedirectResponse(
        url=f"trex://auth/callback?token={access_token}"
    )


@router.get("/me")
def get_current_user_info(
    current_user=Depends(get_current_user)
):

    return {
        "id": current_user.id,
        "name": current_user.name,
        "email": current_user.email,
        "google_id": current_user.google_id
    }