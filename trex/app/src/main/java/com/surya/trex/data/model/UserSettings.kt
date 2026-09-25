package com.surya.trex.data.model

data class UserSettings(
    val notifications_enabled: Boolean,
    val sms_detection_enabled: Boolean,
    val detection_mode: String,
    val created_at: String,
    val updated_at: String
)

data class SettingsUpdateRequest(
    val notifications_enabled: Boolean? = null,
    val sms_detection_enabled: Boolean? = null,
    val detection_mode: String? = null
)

data class TransactionSource(
    val id: Int,
    val app_name: String,
    val package_name: String?,
    val source_type: String,
    val sender_ids: List<String>,
    val enabled: Boolean
)

data class TransactionSourceCreateRequest(
    val app_name: String,
    val package_name: String?,
    val source_type: String,
    val sender_ids: List<String>?,
    val enabled: Boolean = true
)

data class TransactionSourceUpdateRequest(
    val app_name: String? = null,
    val package_name: String? = null,
    val source_type: String? = null,
    val sender_ids: List<String>? = null,
    val enabled: Boolean? = null
)