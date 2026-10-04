package com.surya.trex.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Kept for compatibility with existing code.
 *
 * Authentication and all other API calls use the same backend.
 */
object ApiClient {

    private const val BASE_URL =
        "https://trex-backend-6yna.onrender.com/"

    val authapi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}
