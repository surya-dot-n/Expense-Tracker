package com.surya.trex.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class AuthViewModel (application: Application) : AndroidViewModel(application){
        private val repository = AuthRepository()
        private val tokenManager = TokenManager(application)

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading

        private val _loginSuccess = MutableStateFlow(false)
        val loginSuccess: StateFlow<Boolean> = _loginSuccess

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage

    fun login(email: String, password: String){
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = repository.login(
                    email = email,
                    password = password
                )

                tokenManager.saveToken(response.access_token)

                _loginSuccess.value = true
            } catch (e: Exception){
                _errorMessage.value = e.message ?: "Login failed"
            }
            finally {
                _isLoading.value = false
            }
        }
    }
    fun logout(){
        tokenManager.clearToken()
        _loginSuccess.value = false
    }

    }