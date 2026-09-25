package com.surya.trex.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.UserProfile
import com.surya.trex.data.model.UserProfileUpdate
import com.surya.trex.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(application)

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()


    // ==========================================
    // Load Profile
    // ==========================================

    fun loadProfile() {

        if (_isLoading.value) {
            return
        }

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            try {

                val token = tokenManager.getToken()

                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Authentication token not found"
                    return@launch
                }

                val response = apiService.getMyProfile(
                    "Bearer $token"
                )

                _profile.value = response

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message ?: "Failed to load profile"

            } finally {

                _isLoading.value = false
            }
        }
    }


    // ==========================================
    // Update Name
    // ==========================================

    fun updateName(name: String) {

        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            _errorMessage.value = "Name cannot be empty"
            return
        }

        viewModelScope.launch {

            _isSaving.value = true
            _errorMessage.value = null

            try {

                val token = tokenManager.getToken()

                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Authentication token not found"
                    return@launch
                }

                val request = UserProfileUpdate(
                    name = trimmedName
                )

                val response = apiService.updateMyProfile(
                    token = "Bearer $token",
                    request = request
                )

                _profile.value = response

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message ?: "Failed to update profile"

            } finally {

                _isSaving.value = false
            }
        }
    }


    // ==========================================
    // Clear Error
    // ==========================================

    fun clearError() {
        _errorMessage.value = null
    }
}