package com.surya.trex.data.local

import android.content.Context

class ThemeManager(context: Context) {

    private val preferences =
        context.getSharedPreferences(
            "theme_prefs",
            Context.MODE_PRIVATE
        )

    companion object {

        private const val THEME_KEY =
            "theme"
    }

    fun saveTheme(
        theme: String
    ) {

        preferences
            .edit()
            .putString(
                THEME_KEY,
                theme
            )
            .apply()
    }

    fun getTheme(): String {

        return preferences.getString(
            THEME_KEY,
            "SYSTEM"
        ) ?: "SYSTEM"
    }
}