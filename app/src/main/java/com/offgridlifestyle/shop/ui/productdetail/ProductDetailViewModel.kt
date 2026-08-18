package com.offgridlifestyle.shop.ui.productdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offgridlifestyle.shop.data.cart.CartRepository
import com.offgridlifestyle.shop.data.model.Product
import com.offgridlifestyle.shop.data.model.ProductVariant
import com.offgridlifestyle.shop.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val selectedVariant: ProductVariant? = null,
    val quantity: Int = 1,
    val justAddedToCart: Boolean = false,
    val errorMessage: String? = null
)

class ProductDetailViewModel(
    private val handle: String,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            productRepository.getProductByHandle(handle)
                .onSuccess { product ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        product = product,
                        selectedVariant = product?.variants?.firstOrNull { it.availableForSale }
                            ?: product?.variants?.firstOrNull()
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Couldn't load this product"
                    )
                }
        }
    }

    fun onVariantSelected(variant: ProductVariant) {
        _uiState.value = _uiState.value.copy(selectedVariant = variant, justAddedToCart = false)
    }

    fun onQuantityChanged(quantity: Int) {
        if (quantity in 1..99) {
            _uiState.value = _uiState.value.copy(quantity = quantity, justAddedToCart = false)
        }
    }

    fun addToCart() {
        val state = _uiState.value
        val product = state.product ?: return
        val variant = state.selectedVariant ?: return
        cartRepository.addItem(product, variant, state.quantity)
        _uiState.value = state.copy(justAddedToCart = true, quantity = 1)
    }
}
