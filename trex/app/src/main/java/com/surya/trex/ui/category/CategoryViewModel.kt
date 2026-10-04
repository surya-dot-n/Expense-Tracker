package com.surya.trex.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.Category
import com.surya.trex.data.repository.CategoryRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

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
    // ERROR
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
    // LOAD CATEGORIES
    // ============================================================

    fun loadCategories() {

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            repository.getCategories()
                .onSuccess { result ->

                    _categories.value = result

                }
                .onFailure { error ->

                    _errorMessage.value =
                        error.message ?: "Failed to load categories"
                }

            _isLoading.value = false
        }
    }


    // ============================================================
    // ADD CATEGORY
    // ============================================================

    fun addCategory(
        categoryName: String,
        categoryType: String
    ) {

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            repository.addCategory(
                categoryName = categoryName,
                categoryType = categoryType
            )
                .onSuccess {

                    _successMessage.value =
                        "Category added successfully"

                    // Refresh list
                    loadCategories()
                }
                .onFailure { error ->

                    _errorMessage.value =
                        error.message ?: "Failed to add category"
                }

            _isLoading.value = false
        }
    }


    // ============================================================
    // UPDATE CATEGORY
    // ============================================================

    fun updateCategory(
        categoryId: Int,
        categoryName: String,
        categoryType: String
    ) {

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            repository.updateCategory(
                categoryId = categoryId,
                categoryName = categoryName,
                categoryType = categoryType
            )
                .onSuccess {

                    _successMessage.value =
                        "Category updated successfully"

                    // Refresh list
                    loadCategories()
                }
                .onFailure { error ->

                    _errorMessage.value =
                        error.message ?: "Failed to update category"
                }

            _isLoading.value = false
        }
    }


    // ============================================================
    // DELETE CATEGORY
    // ============================================================

    fun deleteCategory(
        categoryId: Int
    ) {

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            repository.deleteCategory(
                categoryId = categoryId
            )
                .onSuccess {

                    _successMessage.value =
                        "Category deleted successfully"

                    // Refresh list
                    loadCategories()
                }
                .onFailure { error ->

                    _errorMessage.value =
                        error.message ?: "Failed to delete category"
                }

            _isLoading.value = false
        }
    }


    // ============================================================
    // CLEAR ERROR
    // ============================================================

    fun clearError() {
        _errorMessage.value = null
    }


    // ============================================================
    // CLEAR SUCCESS
    // ============================================================

    fun clearSuccess() {
        _successMessage.value = null
    }
}


// ================================================================
// VIEWMODEL FACTORY
// ================================================================

class CategoryViewModelFactory(
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {

            return CategoryViewModel(
                repository = CategoryRepository(tokenManager)
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
