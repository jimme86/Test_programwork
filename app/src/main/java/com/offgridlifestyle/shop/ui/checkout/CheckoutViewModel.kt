package com.offgridlifestyle.shop.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offgridlifestyle.shop.data.cart.CartRepository
import com.offgridlifestyle.shop.data.model.OrderRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CheckoutFormState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val addressLine: String = "",
    val city: String = "",
    val postalCode: String = "",
    val country: String = "",
    val notes: String = ""
) {
    val isValid: Boolean
        get() = fullName.isNotBlank() &&
            email.contains("@") &&
            addressLine.isNotBlank() &&
            city.isNotBlank() &&
            postalCode.isNotBlank() &&
            country.isNotBlank()
}

sealed interface CheckoutSubmitState {
    data object Idle : CheckoutSubmitState
    data object Submitting : CheckoutSubmitState
    data class Submitted(val order: OrderRequest) : CheckoutSubmitState
}

/**
 * Collects order details for the cart. No payment provider is wired up yet
 * (browse + cart scope) — submitting records the order request so the store
 * owner can follow up, then clears the cart. Swap [submit] for a real
 * payment/checkout API call when one is available.
 */
class CheckoutViewModel(private val cartRepository: CartRepository) : ViewModel() {

    private val _formState = MutableStateFlow(CheckoutFormState())
    val formState: StateFlow<CheckoutFormState> = _formState.asStateFlow()

    private val _submitState = MutableStateFlow<CheckoutSubmitState>(CheckoutSubmitState.Idle)
    val submitState: StateFlow<CheckoutSubmitState> = _submitState.asStateFlow()

    fun onFieldChanged(update: (CheckoutFormState) -> CheckoutFormState) {
        _formState.value = update(_formState.value)
    }

    fun submit() {
        val items = cartRepository.cartItems.value
        val form = _formState.value
        if (!form.isValid || items.isEmpty()) return

        viewModelScope.launch {
            _submitState.value = CheckoutSubmitState.Submitting
            delay(700) // placeholder for a real order/checkout API call
            val order = OrderRequest(
                fullName = form.fullName,
                email = form.email,
                phone = form.phone,
                addressLine = form.addressLine,
                city = form.city,
                postalCode = form.postalCode,
                country = form.country,
                notes = form.notes,
                items = items,
                subtotal = items.sumOf { it.lineTotal },
                currencyCode = items.firstOrNull()?.currencyCode ?: "EUR"
            )
            cartRepository.clear()
            _submitState.value = CheckoutSubmitState.Submitted(order)
        }
    }
}
