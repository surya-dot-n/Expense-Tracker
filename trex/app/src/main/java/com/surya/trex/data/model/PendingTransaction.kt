package com.surya.trex.data.model

data class PendingTransaction(
    val id: Int,
    val source_id: Int?,
    val app_name: String?,
    val package_name: String?,
    val source_type: String,
    val sender_id: String?,
    val message_body: String?,
    val amount: String,
    val transaction_type: String,
    val description: String?,
    val category_id: Int?,
    val transaction_date: String?,
    val confidence: String?,
    val fingerprint: String?,
    val status: String,
    val detected_at: String,
    val created_at: String,
    val updated_at: String
)