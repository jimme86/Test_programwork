package com.offgridlifestyle.shop.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offgridlifestyle.shop.data.model.Product
import com.offgridlifestyle.shop.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredProducts: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val isLiveStore: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLiveStore = repository.isLiveStore))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val productsResult = repository.getProducts()
            val categoriesResult = repository.getCategories()

            productsResult
                .onSuccess { products ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        featuredProducts = products.take(8),
                        categories = categoriesResult.getOrDefault(emptyList())
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Something went wrong"
                    )
                }
        }
    }
}
