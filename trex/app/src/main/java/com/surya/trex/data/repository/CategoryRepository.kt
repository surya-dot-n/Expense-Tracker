package com.surya.trex.data.repository

import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.Category
import com.surya.trex.data.model.CategoryCreateRequest
import com.surya.trex.data.model.CategoryUpdateRequest
import com.surya.trex.data.remote.RetrofitClient

class CategoryRepository(
    private val tokenManager: TokenManager
) {

    private suspend fun getAuthToken(): Result<String> {

        val token = tokenManager.getToken()

        return if (token.isNullOrEmpty()) {
            Result.failure(
                Exception("User is not logged in")
            )
        } else {
            Result.success("Bearer $token")
        }
    }

    // ============================================================
    // GET ALL CATEGORIES
    // ============================================================

    suspend fun getCategories(): Result<List<Category>> {

        val authResult = getAuthToken()

        if (authResult.isFailure) {
            return Result.failure(
                authResult.exceptionOrNull()
                    ?: Exception("Authentication failed")
            )
        }

        val token = authResult.getOrThrow()

        return try {

            val response = RetrofitClient.apiService.getCategories(
                token = token
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

    // ============================================================
    // CREATE CATEGORY
    // ============================================================

    suspend fun addCategory(
        categoryName: String,
        categoryType: String
    ): Result<Category> {

        val authResult = getAuthToken()

        if (authResult.isFailure) {
            return Result.failure(
                authResult.exceptionOrNull()
                    ?: Exception("Authentication failed")
            )
        }

        val token = authResult.getOrThrow()

        return try {

            val request = CategoryCreateRequest(
                category_name = categoryName,
                category_type = categoryType
            )

            val response = RetrofitClient.apiService.createCategory(
                token = token,
                request = request
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

    // ============================================================
    // UPDATE CATEGORY
    // ============================================================

    suspend fun updateCategory(
        categoryId: Int,
        categoryName: String,
        categoryType: String
    ): Result<Category> {

        val authResult = getAuthToken()

        if (authResult.isFailure) {
            return Result.failure(
                authResult.exceptionOrNull()
                    ?: Exception("Authentication failed")
            )
        }

        val token = authResult.getOrThrow()

        return try {

            val request = CategoryUpdateRequest(
                category_name = categoryName,
                category_type = categoryType
            )

            val response = RetrofitClient.apiService.updateCategory(
                token = token,
                categoryId = categoryId,
                request = request
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

    // ============================================================
    // DELETE CATEGORY
    // ============================================================

    suspend fun deleteCategory(
        categoryId: Int
    ): Result<Unit> {

        val authResult = getAuthToken()

        if (authResult.isFailure) {
            return Result.failure(
                authResult.exceptionOrNull()
                    ?: Exception("Authentication failed")
            )
        }

        val token = authResult.getOrThrow()

        return try {

            val response = RetrofitClient.apiService.deleteCategory(
                token = token,
                categoryId = categoryId
            )

            if (response.isSuccessful) {

                Result.success(Unit)

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
