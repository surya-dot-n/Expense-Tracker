package com.surya.trex.data.repository

import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.Transaction
import com.surya.trex.data.model.TransactionCreateRequest
import com.surya.trex.data.remote.RetrofitClient

class TransactionRepository(
    private val tokenManager: TokenManager
) {

    private val apiService =
        RetrofitClient.apiService


    // ==========================================
    // Create Transaction
    // ==========================================

    suspend fun createTransaction(
        request: Transaction
    ): Result<Transaction> {

        val token = tokenManager.getToken()

        if (token.isNullOrEmpty()) {
            return Result.failure(
                Exception("User is not logged in")
            )
        }

        return try {

            val response =
                apiService.createTransaction(
                    token = "Bearer $token",
                    request = TransactionCreateRequest(
                        category_id = request.category_id,
                        amount = request.amount,
                        description = request.description,
                        transaction_type = request.transaction_type,
                        transaction_date = request.transaction_date,
                        transaction_time = request.transaction_time
                    )
                )

            if (
                response.isSuccessful &&
                response.body() != null
            ) {

                Result.success(
                    response.body()!!
                )

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


    // ==========================================
    // Get All Transactions
    // ==========================================

    suspend fun getTransactions(): Result<List<Transaction>> {

        val token = tokenManager.getToken()

        if (token.isNullOrEmpty()) {
            return Result.failure(
                Exception("User is not logged in")
            )
        }

        return try {

            val response =
                apiService.getTransactions(
                    token = "Bearer $token"
                )

            if (
                response.isSuccessful &&
                response.body() != null
            ) {

                Result.success(
                    response.body()!!
                )

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