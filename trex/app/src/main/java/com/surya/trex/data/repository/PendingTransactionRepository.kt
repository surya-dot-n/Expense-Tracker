package com.surya.trex.data.repository

import com.surya.trex.data.model.PendingTransaction
import com.surya.trex.data.model.PendingTransactionCreateRequest
import com.surya.trex.data.remote.RetrofitClient

class PendingTransactionRepository {

    private val apiService =
        RetrofitClient.apiService

    suspend fun getPendingTransactions(
        token: String
    ): Result<List<PendingTransaction>> {

        return try {
            val response =
                apiService.getPendingTransactions(
                    token = "Bearer $token"
                )

            if (response.isSuccessful) {
                Result.success(
                    response.body() ?: emptyList()
                )
            } else {
                Result.failure(
                    Exception(
                        getErrorMessage(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(
                Exception(
                    e.message ?: "Unable to load pending transactions."
                )
            )
        }
    }

    suspend fun createPendingTransaction(
        token: String,
        request: PendingTransactionCreateRequest
    ): Result<PendingTransaction> {

        return try {
            val normalizedRequest =
                request.copy(
                    transaction_type =
                        request.transaction_type
                            .trim()
                            .lowercase()
                )

            val response =
                apiService.createPendingTransaction(
                    token = "Bearer $token",
                    request = normalizedRequest
                )

            if (response.isSuccessful) {

                val pending = response.body()

                if (pending != null) {
                    Result.success(pending)
                } else {
                    Result.failure(
                        Exception(
                            "Server returned an empty pending transaction."
                        )
                    )
                }

            } else {
                Result.failure(
                    Exception(
                        getErrorMessage(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(
                Exception(
                    e.message ?: "Unable to create pending transaction."
                )
            )
        }
    }

    suspend fun approvePendingTransaction(
        token: String,
        pendingId: Int
    ): Result<String> {

        return try {
            val response =
                apiService.approvePendingTransaction(
                    token = "Bearer $token",
                    pendingId = pendingId
                )

            if (response.isSuccessful) {

                val body = response.body()

                Result.success(
                    body?.get("message")?.toString()
                        ?: "Transaction approved."
                )

            } else {
                Result.failure(
                    Exception(
                        getErrorMessage(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(
                Exception(
                    e.message ?: "Unable to approve transaction."
                )
            )
        }
    }

    suspend fun denyPendingTransaction(
        token: String,
        pendingId: Int
    ): Result<String> {

        return try {
            val response =
                apiService.denyPendingTransaction(
                    token = "Bearer $token",
                    pendingId = pendingId
                )

            if (response.isSuccessful) {

                val body = response.body()

                Result.success(
                    body?.get("message")?.toString()
                        ?: "Transaction denied."
                )

            } else {
                Result.failure(
                    Exception(
                        getErrorMessage(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(
                Exception(
                    e.message ?: "Unable to deny transaction."
                )
            )
        }
    }

    suspend fun deletePendingTransaction(
        token: String,
        pendingId: Int
    ): Result<String> {

        return try {
            val response =
                apiService.deletePendingTransaction(
                    token = "Bearer $token",
                    pendingId = pendingId
                )

            if (response.isSuccessful) {

                val body = response.body()

                Result.success(
                    body?.get("message")?.toString()
                        ?: "Pending transaction deleted."
                )

            } else {
                Result.failure(
                    Exception(
                        getErrorMessage(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(
                Exception(
                    e.message ?: "Unable to delete pending transaction."
                )
            )
        }
    }

    private fun getErrorMessage(
        statusCode: Int,
        errorBody: String?
    ): String {

        val cleanError =
            errorBody
                ?.replace("\n", " ")
                ?.trim()

        return if (!cleanError.isNullOrBlank()) {
            "Server error $statusCode: $cleanError"
        } else {
            "Server error $statusCode."
        }
    }
}
