package com.offgridlifestyle.shop.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offgridlifestyle.shop.data.model.Product
import com.offgridlifestyle.shop.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CatalogUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class CatalogViewModel(
    private val repository: ProductRepository,
    initialCategory: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState(selectedCategory = initialCategory))
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCategories().onSuccess { cats ->
                _uiState.value = _uiState.value.copy(categories = cats)
            }
        }
        refresh()
    }

    fun onCategorySelected(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        refresh()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val state = _uiState.value
            repository.getProducts(
                category = state.selectedCategory,
                searchQuery = state.searchQuery.ifBlank { null }
            ).onSuccess { products ->
                _uiState.value = _uiState.value.copy(isLoading = false, products = products)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Couldn't load products"
                )
            }
        }
    }
}
