package com.surya.trex.data.remote

import com.surya.trex.data.model.AuthModels
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(
        @Body request: AuthModels.LoginRequest
    ): AuthModels.TokenResponse

}