package com.surya.trex.data.model

data class DashboardSummary(
    val total_income: Double,
    val total_expense: Double,
    val balance: Double
)

data class CategorySummary(
    val category_id: Int,
    val category_name: String,
    val total_amount: Double
)

data class RecentTransaction(
    val id: Int,
    val category_id: Int,
    val amount: Double,
    val description: String?,
    val transaction_type: String
)