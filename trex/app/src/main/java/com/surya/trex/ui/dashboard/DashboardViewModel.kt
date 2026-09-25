package com.surya.trex.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.CategorySummary
import com.surya.trex.data.model.DashboardSummary
import com.surya.trex.data.model.RecentTransaction
import com.surya.trex.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val tokenManager: TokenManager
): ViewModel() {
    private val _summary = MutableStateFlow<DashboardSummary?>(null)
    val summary: StateFlow<DashboardSummary?> = _summary

    private val _categories = MutableStateFlow<List<CategorySummary>>(emptyList())
    val categories: StateFlow<List<CategorySummary>> = _categories

    private val _recentTransactions = MutableStateFlow<List<RecentTransaction>>(emptyList())
    val recentTransactions: StateFlow<List<RecentTransaction>> = _recentTransactions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadDashboard(){
        val token = tokenManager.getToken()
        if(token == null){
            _error.value = "User is not logged in"
            return
        }
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch{
            try{
                val authorizationToken = "Bearer $token"

                val summaryResponse = RetrofitClient.apiService.getDashboardSummary(authorizationToken)
                val categoriesResponse = RetrofitClient.apiService.getDashboardCategories(authorizationToken)
                val recentResponse = RetrofitClient.apiService.getRecentTransactions(authorizationToken)

                if(summaryResponse.isSuccessful && categoriesResponse.isSuccessful && recentResponse.isSuccessful){
                    _summary.value = summaryResponse.body()
                    _categories.value = categoriesResponse.body() ?: emptyList()
                    _recentTransactions.value = recentResponse.body() ?: emptyList()
                }
                else {
                    _error.value = "Failed to load dashboard"
                }
            }
            catch(e: Exception){
                _error.value = e.message ?: "Something went Wrong"
            }
            finally {
                _isLoading.value = false
            }
        }
    }
}