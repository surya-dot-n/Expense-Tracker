package com.surya.trex.data.repository

import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.CategorySummary
import com.surya.trex.data.model.DashboardSummary
import com.surya.trex.data.model.RecentTransaction
import com.surya.trex.data.remote.RetrofitClient

class DashboardRepository(
    private val tokenManager: TokenManager
) {

    /**
     * Builds dashboard data from the same /transactions/ endpoint used by
     * the Transactions screen.
     *
     * This keeps dashboard totals consistent with the transaction list and
     * avoids depending on a server-side dashboard aggregation that may use
     * different filtering/casing rules.
     */
    private suspend fun getAllTransactions() =
        RetrofitClient.apiService.getTransactions(
            token = "Bearer ${tokenManager.getToken().orEmpty()}"
        )

    private suspend fun getCategories() =
        RetrofitClient.apiService.getCategories(
            token = "Bearer ${tokenManager.getToken().orEmpty()}"
        )

    private fun <T> authCheck(
        block: suspend () -> retrofit2.Response<T>
    ): suspend () -> Result<T> = {
        val token = tokenManager.getToken()
        if (token.isNullOrBlank()) {
            Result.failure(Exception("User is not logged in"))
        } else {
            try {
                val response = block()
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

    suspend fun getDashboardSummary(): Result<DashboardSummary> {
        val result = authCheck { getAllTransactions() }()
        return result.map { transactions ->
            val income = transactions
                .filter { it.transaction_type.equals("income", ignoreCase = true) }
                .sumOf { it.amount }

            val expense = transactions
                .filter { it.transaction_type.equals("expense", ignoreCase = true) }
                .sumOf { it.amount }

            DashboardSummary(
                total_income = income,
                total_expense = expense,
                balance = income - expense
            )
        }
    }

    suspend fun getDashboardCategories(): Result<List<CategorySummary>> {
        val transactionsResult = authCheck { getAllTransactions() }()
        if (transactionsResult.isFailure) {
            return Result.failure(transactionsResult.exceptionOrNull()!!)
        }

        val categoriesResult = authCheck { getCategories() }()
        if (categoriesResult.isFailure) {
            return Result.failure(categoriesResult.exceptionOrNull()!!)
        }

        val transactions = transactionsResult.getOrThrow()
        val categories = categoriesResult.getOrThrow()

        val categoryNames = categories.associateBy { it.id }

        val result = transactions
            .asSequence()
            .filter {
                it.transaction_type.equals("expense", ignoreCase = true)
            }
            .groupBy { it.category_id }
            .map { (categoryId, items) ->
                CategorySummary(
                    category_id = categoryId,
                    category_name =
                        categoryNames[categoryId]?.category_name
                            ?: "Other",
                    total_amount = items.sumOf { it.amount }
                )
            }
            .sortedByDescending { it.total_amount }

        return Result.success(result)
    }

    suspend fun getRecentTransactions(): Result<List<RecentTransaction>> {
        val result = authCheck { getAllTransactions() }()

        return result.map { transactions ->
            transactions
                .sortedWith(
                    compareByDescending<com.surya.trex.data.model.Transaction> {
                        it.transaction_date.orEmpty()
                    }.thenByDescending {
                        it.transaction_time.orEmpty()
                    }
                )
                .take(5)
                .map { transaction ->
                    RecentTransaction(
                        id = transaction.id ?: 0,
                        category_id = transaction.category_id,
                        amount = transaction.amount,
                        description = transaction.description,
                        transaction_type = transaction.transaction_type.lowercase()
                    )
                }
        }
    }
}
