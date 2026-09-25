package com.surya.trex.data.repository

import com.surya.trex.data.model.AuthModels.LoginRequest
import com.surya.trex.data.model.AuthModels.TokenResponse
import com.surya.trex.data.remote.ApiClient
class AuthRepository {
    private val authApi = ApiClient.authapi

    suspend fun login(
        email: String,
        password: String
    ): TokenResponse{
        val request = LoginRequest(email = email, password = password)
        return authApi.login(request)
    }
}