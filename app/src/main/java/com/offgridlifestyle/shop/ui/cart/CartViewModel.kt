package com.offgridlifestyle.shop.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offgridlifestyle.shop.data.cart.CartRepository
import com.offgridlifestyle.shop.data.model.CartItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val currencyCode: String = "EUR",
    val itemCount: Int = 0
)

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    val uiState: StateFlow<CartUiState> = cartRepository.cartItems
        .map { items ->
            CartUiState(
                items = items,
                subtotal = items.sumOf { it.lineTotal },
                currencyCode = items.firstOrNull()?.currencyCode ?: "EUR",
                itemCount = items.sumOf { it.quantity }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    fun updateQuantity(variantId: String, quantity: Int) {
        cartRepository.updateQuantity(variantId, quantity)
    }

    fun removeItem(variantId: String) {
        cartRepository.removeItem(variantId)
    }

    fun clearCart() {
        cartRepository.clear()
    }
}
