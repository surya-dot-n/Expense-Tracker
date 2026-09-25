package com.surya.trex.data.model

data class SmsTransactionRequest(
    val amount: String,
    val transaction_type: String,
    val description: String?,
    val merchant: String?,
    val sms_sender: String,
    val sms_body: String,
    val timestamp: Long,
    val source_app: String?,
    val source_package: String?
)