package com.surya.trex.data.model

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val google_id: String
)
data class UserProfileUpdate(
    val name: String
)