package com.surya.trex.data.model

data class Transaction(
    val id: Int? = null,
    val user_id: Int? = null,
    val category_id: Int,
    val amount: Double,
    val description: String? = null,
    val transaction_type: String,
    val transaction_date: String? = null,
    val transaction_time: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class TransactionCreateRequest(
    val category_id: Int,
    val amount: Double,
    val description: String? = null,
    val transaction_type: String,
    val transaction_date: String? = null,
    val transaction_time: String? = null
)