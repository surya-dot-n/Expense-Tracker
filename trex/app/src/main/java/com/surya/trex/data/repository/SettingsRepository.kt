package com.surya.trex.data.repository

import com.surya.trex.data.model.SettingsUpdateRequest
import com.surya.trex.data.model.TransactionSource
import com.surya.trex.data.model.TransactionSourceCreateRequest
import com.surya.trex.data.model.TransactionSourceUpdateRequest
import com.surya.trex.data.model.UserSettings
import com.surya.trex.data.remote.RetrofitClient

class SettingsRepository {

    private val apiService =
        RetrofitClient.apiService


    // ==========================================
    // Settings
    // ==========================================

    suspend fun getSettings(
        token: String
    ): UserSettings {

        val response =
            apiService.getSettings(
                token = "Bearer $token"
            )

        if (!response.isSuccessful) {
            throw Exception(
                "Failed to load settings: ${response.code()}"
            )
        }

        return response.body()
            ?: throw Exception(
                "Settings response is empty."
            )
    }


    suspend fun updateSettings(
        token: String,
        request: SettingsUpdateRequest
    ): UserSettings {

        val response =
            apiService.updateSettings(
                token = "Bearer $token",
                request = request
            )

        if (!response.isSuccessful) {
            throw Exception(
                "Failed to update settings: ${response.code()}"
            )
        }

        return response.body()
            ?: throw Exception(
                "Updated settings response is empty."
            )
    }


    // ==========================================
    // Sources
    // ==========================================

    suspend fun getSources(
        token: String
    ): List<TransactionSource> {

        val response =
            apiService.getTransactionSources(
                token = "Bearer $token"
            )

        if (!response.isSuccessful) {
            throw Exception(
                "Failed to load transaction sources: ${response.code()}"
            )
        }

        return response.body()
            ?: emptyList()
    }


    suspend fun createSource(
        token: String,
        request: TransactionSourceCreateRequest
    ): TransactionSource {

        val response =
            apiService.createTransactionSource(
                token = "Bearer $token",
                request = request
            )

        if (!response.isSuccessful) {
            throw Exception(
                "Failed to create source: ${response.code()}"
            )
        }

        return response.body()
            ?: throw Exception(
                "Created source response is empty."
            )
    }


    suspend fun updateSource(
        token: String,
        sourceId: Int,
        request: TransactionSourceUpdateRequest
    ): TransactionSource {

        val response =
            apiService.updateTransactionSource(
                token = "Bearer $token",
                sourceId = sourceId,
                request = request
            )

        if (!response.isSuccessful) {
            throw Exception(
                "Failed to update source: ${response.code()}"
            )
        }

        return response.body()
            ?: throw Exception(
                "Updated source response is empty."
            )
    }


    suspend fun deleteSource(
        token: String,
        sourceId: Int
    ) {

        val response =
            apiService.deleteTransactionSource(
                token = "Bearer $token",
                sourceId = sourceId
            )

        if (!response.isSuccessful) {
            throw Exception(
                "Failed to delete source: ${response.code()}"
            )
        }
    }
}