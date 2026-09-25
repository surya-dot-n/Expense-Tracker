package com.surya.trex.data.repository

import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.Category
import com.surya.trex.data.remote.RetrofitClient

class CategoryRepository(
    private val tokenManager: TokenManager
) {

    suspend fun getCategories(): Result<List<Category>> {

        val token = tokenManager.getToken()

        if (token.isNullOrEmpty()) {
            return Result.failure(
                Exception("User is not logged in")
            )
        }

        return try {

            val response = RetrofitClient.apiService.getCategories(
                token = "Bearer $token"
            )

            if (response.isSuccessful && response.body() != null) {

                Result.success(response.body()!!)

            } else {

                Result.failure(
                    Exception(
                        "Failed: ${response.code()} ${response.message()}"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}