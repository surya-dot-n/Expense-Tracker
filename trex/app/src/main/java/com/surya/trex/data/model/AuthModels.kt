package com.surya.trex.data.model

class AuthModels {
    data class LoginRequest(
        val email: String,
        val password: String
    )
    data class TokenResponse(
        val access_token: String,
        val token_type: String
    )
}