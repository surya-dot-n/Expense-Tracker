package com.surya.trex.ui.pending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.Category
import com.surya.trex.data.model.PendingTransaction
import com.surya.trex.data.remote.RetrofitClient
import com.surya.trex.data.repository.PendingTransactionRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PendingTransactionViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val pendingRepository =
        PendingTransactionRepository()

    private val apiService =
        RetrofitClient.apiService

    // ============================================================
    // PENDING TRANSACTIONS
    // ============================================================

    private val _pendingTransactions =
        MutableStateFlow<List<PendingTransaction>>(emptyList())

    val pendingTransactions: StateFlow<List<PendingTransaction>> =
        _pendingTransactions.asStateFlow()


    // ============================================================
    // CATEGORIES
    // ============================================================

    private val _categories =
        MutableStateFlow<List<Category>>(emptyList())

    val categories: StateFlow<List<Category>> =
        _categories.asStateFlow()


    // ============================================================
    // LOADING
    // ============================================================

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    // ============================================================
    // APPROVING
    // ============================================================

    private val _isApproving =
        MutableStateFlow(false)

    val isApproving: StateFlow<Boolean> =
        _isApproving.asStateFlow()


    // ============================================================
    // DENYING
    // ============================================================

    private val _isDenying =
        MutableStateFlow(false)

    val isDenying: StateFlow<Boolean> =
        _isDenying.asStateFlow()


    // ============================================================
    // ERROR MESSAGE
    // ============================================================

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()


    // ============================================================
    // SUCCESS MESSAGE
    // ============================================================

    private val _successMessage =
        MutableStateFlow<String?>(null)

    val successMessage: StateFlow<String?> =
        _successMessage.asStateFlow()


    // ============================================================
    // INITIAL LOAD
    // ============================================================

    init {
        loadPendingTransactions()
        loadCategories()
    }


    // ============================================================
    // LOAD PENDING TRANSACTIONS
    // ============================================================

    fun loadPendingTransactions() {

        val token =
            tokenManager.getToken()

        if (token.isNullOrBlank()) {

            _errorMessage.value =
                "Authentication token not found."

            return
        }

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            pendingRepository
                .getPendingTransactions(token)
                .onSuccess { transactions ->

                    _pendingTransactions.value =
                        transactions
                }
                .onFailure { error ->

                    _errorMessage.value =
                        error.message
                            ?: "Failed to load pending transactions."
                }

            _isLoading.value = false
        }
    }


    // ============================================================
    // LOAD CATEGORIES
    // ============================================================

    private fun loadCategories() {

        val token =
            tokenManager.getToken()

        if (token.isNullOrBlank()) {
            return
        }

        viewModelScope.launch {

            try {

                val response =
                    apiService.getCategories(
                        token = "Bearer $token"
                    )

                if (response.isSuccessful) {

                    _categories.value =
                        response.body()
                            ?: emptyList()

                } else {

                    _errorMessage.value =
                        "Failed to load categories: ${response.code()}"
                }

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message
                        ?: "Failed to load categories."
            }
        }
    }


    // ============================================================
    // UPDATE CATEGORY
    // ============================================================

    private suspend fun updateCategory(
        token: String,
        pendingId: Int,
        categoryId: Int
    ): Result<Unit> {

        return try {

            val response =
                apiService.updatePendingTransactionCategory(
                    token = "Bearer $token",
                    pendingId = pendingId,
                    categoryId = categoryId
                )

            if (response.isSuccessful) {

                Result.success(Unit)

            } else {

                Result.failure(
                    Exception(
                        "Failed to select category: ${response.code()}"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // ============================================================
    // APPROVE TRANSACTION
    // ============================================================

    fun approveTransaction(
        pendingId: Int,
        categoryId: Int
    ) {

        val token =
            tokenManager.getToken()

        if (token.isNullOrBlank()) {

            _errorMessage.value =
                "Authentication token not found."

            return
        }

        viewModelScope.launch {

            _isApproving.value = true

            _errorMessage.value = null
            _successMessage.value = null

            // ----------------------------------------------------
            // First assign category
            // ----------------------------------------------------

            val categoryResult =
                updateCategory(
                    token = token,
                    pendingId = pendingId,
                    categoryId = categoryId
                )

            if (categoryResult.isFailure) {

                _errorMessage.value =
                    categoryResult
                        .exceptionOrNull()
                        ?.message
                        ?: "Failed to select category."

                _isApproving.value = false

                return@launch
            }

            // ----------------------------------------------------
            // Then approve
            // ----------------------------------------------------

            pendingRepository
                .approvePendingTransaction(
                    token = token,
                    pendingId = pendingId
                )
                .onSuccess { message ->

                    _successMessage.value =
                        message

                    loadPendingTransactions()
                }
                .onFailure { error ->

                    _errorMessage.value =
                        error.message
                            ?: "Failed to approve transaction."
                }

            _isApproving.value = false
        }
    }


    // ============================================================
    // DENY TRANSACTION
    // ============================================================

    fun denyTransaction(
        pendingId: Int
    ) {

        val token =
            tokenManager.getToken()

        if (token.isNullOrBlank()) {

            _errorMessage.value =
                "Authentication token not found."

            return
        }

        viewModelScope.launch {

            _isDenying.value = true

            _errorMessage.value = null
            _successMessage.value = null

            pendingRepository
                .denyPendingTransaction(
                    token = token,
                    pendingId = pendingId
                )
                .onSuccess { message ->

                    _successMessage.value =
                        message

                    loadPendingTransactions()
                }
                .onFailure { error ->

                    _errorMessage.value =
                        error.message
                            ?: "Failed to deny transaction."
                }

            _isDenying.value = false
        }
    }


    // ============================================================
    // DELETE TRANSACTION
    // ============================================================

    fun deleteTransaction(
        pendingId: Int
    ) {

        val token =
            tokenManager.getToken()

        if (token.isNullOrBlank()) {

            _errorMessage.value =
                "Authentication token not found."

            return
        }

        viewModelScope.launch {

            _errorMessage.value = null
            _successMessage.value = null

            pendingRepository
                .deletePendingTransaction(
                    token = token,
                    pendingId = pendingId
                )
                .onSuccess { message ->

                    _successMessage.value =
                        message

                    loadPendingTransactions()
                }
                .onFailure { error ->

                    _errorMessage.value =
                        error.message
                            ?: "Failed to delete transaction."
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