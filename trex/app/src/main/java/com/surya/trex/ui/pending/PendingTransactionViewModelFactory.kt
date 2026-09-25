package com.surya.trex.ui.pending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.surya.trex.data.local.TokenManager

class PendingTransactionViewModelFactory(
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                PendingTransactionViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return PendingTransactionViewModel(
                tokenManager = tokenManager
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}