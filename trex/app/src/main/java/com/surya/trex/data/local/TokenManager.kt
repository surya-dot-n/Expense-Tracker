package com.surya.trex.data.local

import android.content.Context
import android.util.Log

class TokenManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "auth_preferences",
        Context.MODE_PRIVATE
    )

    fun saveToken(token: String) {
        preferences.edit()
            .putString("access_token", token)
            .apply()

        Log.d(
            "TREX_TOKEN",
            "JWT token saved successfully"
        )
    }

    fun getToken(): String? {
        val token = preferences.getString(
            "access_token",
            null
        )

        // TEMPORARY: used to get the JWT for Swagger testing
        Log.d(
            "TREX_TOKEN",
            "JWT = $token"
        )

        return token
    }

    fun clearToken() {
        preferences.edit()
            .remove("access_token")
            .apply()

        Log.d(
            "TREX_TOKEN",
            "JWT token cleared"
        )
    }
}