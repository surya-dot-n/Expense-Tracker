package com.surya.trex.ui.settings

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.InstalledApp
import com.surya.trex.data.model.SettingsUpdateRequest
import com.surya.trex.data.model.TransactionSource
import com.surya.trex.data.model.TransactionSourceCreateRequest
import com.surya.trex.data.model.TransactionSourceUpdateRequest
import com.surya.trex.data.model.UserSettings
import com.surya.trex.data.repository.InstalledAppsRepository
import com.surya.trex.data.repository.SettingsRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        SettingsRepository()

    private val tokenManager =
        TokenManager(application)

    private val installedAppsRepository =
        InstalledAppsRepository(application)


    // ============================================================
    // SETTINGS
    // ============================================================

    private val _settings =
        MutableStateFlow<UserSettings?>(null)

    val settings: StateFlow<UserSettings?> =
        _settings


    // ============================================================
    // TRANSACTION SOURCES
    // ============================================================

    private val _sources =
        MutableStateFlow<List<TransactionSource>>(
            emptyList()
        )

    val sources: StateFlow<List<TransactionSource>> =
        _sources


    // ============================================================
    // INSTALLED APPS
    // ============================================================

    private val _installedApps =
        MutableStateFlow<List<InstalledApp>>(
            emptyList()
        )

    val installedApps: StateFlow<List<InstalledApp>> =
        _installedApps


    fun loadInstalledApps() {

        _installedApps.value =
            installedAppsRepository.getInstalledApps()
    }


    // ============================================================
    // LOADING
    // ============================================================

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading


    // ============================================================
    // ERROR
    // ============================================================

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage


    // ============================================================
    // SUCCESS
    // ============================================================

    private val _successMessage =
        MutableStateFlow<String?>(null)

    val successMessage: StateFlow<String?> =
        _successMessage


    // ============================================================
    // LOAD SETTINGS + SOURCES
    // ============================================================
    //
    // IMPORTANT:
    //
    // Settings and sources are loaded independently.
    //
    // If GET /settings returns 422, it MUST NOT prevent
    // GET /settings/sources from executing.
    //
    // ============================================================

    fun loadSettings() {

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            try {

                val token =
                    tokenManager.getToken()

                if (token.isNullOrEmpty()) {

                    throw Exception(
                        "Authentication token not found."
                    )
                }


                // ====================================================
                // LOAD USER SETTINGS
                // ====================================================

                try {

                    _settings.value =
                        repository.getSettings(token)

                } catch (e: Exception) {

                    // Settings failure should NOT stop source loading.
                    _errorMessage.value =
                        e.message
                            ?: "Failed to load settings."

                }


                // ====================================================
                // LOAD TRANSACTION SOURCES
                // ====================================================

                try {

                    val loadedSources =
                        repository.getSources(token)

                    _sources.value =
                        loadedSources

                } catch (e: Exception) {

                    _errorMessage.value =
                        e.message
                            ?: "Failed to load transaction sources."
                }

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message
                        ?: "Failed to load settings."

            } finally {

                _isLoading.value = false
            }
        }
    }


    // ============================================================
    // NOTIFICATIONS
    // ============================================================

    fun setNotificationsEnabled(
        enabled: Boolean
    ) {

        updateSettings(
            SettingsUpdateRequest(
                notifications_enabled = enabled
            )
        )
    }


    // ============================================================
    // SMS DETECTION
    // ============================================================

    fun setSmsDetectionEnabled(
        enabled: Boolean
    ) {

        updateSettings(
            SettingsUpdateRequest(
                sms_detection_enabled = enabled
            )
        )
    }


    // ============================================================
    // DETECTION MODE
    // ============================================================

    fun setDetectionMode(
        mode: String
    ) {

        updateSettings(
            SettingsUpdateRequest(
                detection_mode = mode
            )
        )
    }


    // ============================================================
    // UPDATE SETTINGS
    // ============================================================

    private fun updateSettings(
        request: SettingsUpdateRequest
    ) {

        viewModelScope.launch {

            _errorMessage.value = null

            try {

                val token =
                    tokenManager.getToken()

                if (token.isNullOrEmpty()) {

                    throw Exception(
                        "Authentication token not found."
                    )
                }

                _settings.value =
                    repository.updateSettings(
                        token = token,
                        request = request
                    )

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message
                        ?: "Failed to update settings."
            }
        }
    }


    // ============================================================
    // ADD INSTALLED APP SOURCE
    // ============================================================

    fun addAppSource(
        app: InstalledApp
    ) {

        viewModelScope.launch {

            _errorMessage.value = null
            _successMessage.value = null

            try {

                val token =
                    tokenManager.getToken()

                        ?: throw Exception(
                            "Authentication token not found."
                        )


                // ====================================================
                // CREATE SOURCE
                // ====================================================

                repository.createSource(

                    token = token,

                    request =
                        TransactionSourceCreateRequest(

                            app_name =
                                app.appName,

                            package_name =
                                app.packageName,

                            source_type =
                                "APP_NOTIFICATION",

                            sender_ids =
                                emptyList(),

                            enabled =
                                true
                        )
                )


                // ====================================================
                // SHOW SUCCESS
                // ====================================================

                _successMessage.value =
                    "${app.appName} added successfully."


                // ====================================================
                // RELOAD FROM SERVER
                // ====================================================
                //
                // This makes the Android list match PostgreSQL.
                //
                // ====================================================

                _sources.value =
                    repository.getSources(token)

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message
                        ?: "Failed to add app source."
            }
        }
    }


    // ============================================================
    // ENABLE / DISABLE SOURCE
    // ============================================================

    fun setSourceEnabled(
        source: TransactionSource,
        enabled: Boolean
    ) {

        viewModelScope.launch {

            _errorMessage.value = null

            try {

                val token =
                    tokenManager.getToken()

                if (token.isNullOrEmpty()) {

                    throw Exception(
                        "Authentication token not found."
                    )
                }


                repository.updateSource(

                    token = token,

                    sourceId =
                        source.id,

                    request =
                        TransactionSourceUpdateRequest(
                            enabled = enabled
                        )
                )


                // Reload sources from backend

                _sources.value =
                    repository.getSources(token)

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message
                        ?: "Failed to update source."
            }
        }
    }


    // ============================================================
    // DELETE SOURCE
    // ============================================================

    fun deleteSource(
        source: TransactionSource
    ) {

        viewModelScope.launch {

            _errorMessage.value = null
            _successMessage.value = null

            try {

                val token =
                    tokenManager.getToken()

                if (token.isNullOrEmpty()) {

                    throw Exception(
                        "Authentication token not found."
                    )
                }


                repository.deleteSource(
                    token = token,
                    sourceId = source.id
                )


                _successMessage.value =
                    "${source.app_name} removed."


                // Reload from backend

                _sources.value =
                    repository.getSources(token)

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message
                        ?: "Failed to delete source."
            }
        }
    }


    // ============================================================
    // MANUALLY RELOAD SOURCES
    // ============================================================

    fun loadSources() {

        viewModelScope.launch {

            try {

                val token =
                    tokenManager.getToken()

                if (token.isNullOrEmpty()) {
                    throw Exception(
                        "Authentication token not found."
                    )
                }

                _sources.value =
                    repository.getSources(token)

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message
                        ?: "Failed to load transaction sources."
            }
        }
    }


    // ============================================================
    // CLEAR MESSAGES
    // ============================================================

    fun clearMessages() {

        _errorMessage.value = null
        _successMessage.value = null
    }
}