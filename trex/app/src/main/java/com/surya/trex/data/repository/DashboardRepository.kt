package com.surya.trex.data.repository

import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.CategorySummary
import com.surya.trex.data.model.DashboardSummary
import com.surya.trex.data.model.RecentTransaction
import com.surya.trex.data.remote.RetrofitClient

class DashboardRepository(
    private val tokenManager: TokenManager
) {

    suspend fun getDashboardSummary(): Result<DashboardSummary> {

        val token = tokenManager.getToken()

        if (token.isNullOrEmpty()) {
            return Result.failure(
                Exception("User is not logged in")
            )
        }

        return try {

            val response =
                RetrofitClient.apiService.getDashboardSummary(
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

    suspend fun getDashboardCategories(): Result<List<CategorySummary>> {

        val token = tokenManager.getToken()

        if (token.isNullOrEmpty()) {
            return Result.failure(
                Exception("User is not logged in")
            )
        }

        return try {

            val response =
                RetrofitClient.apiService.getDashboardCategories(
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

    suspend fun getRecentTransactions(): Result<List<RecentTransaction>> {

        val token = tokenManager.getToken()

        if (token.isNullOrEmpty()) {
            return Result.failure(
                Exception("User is not logged in")
            )
        }

        return try {

            val response =
                RetrofitClient.apiService.getRecentTransactions(
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