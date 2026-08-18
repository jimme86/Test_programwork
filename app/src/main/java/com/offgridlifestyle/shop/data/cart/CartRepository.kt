package com.offgridlifestyle.shop.data.cart

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.offgridlifestyle.shop.data.model.CartItem
import com.offgridlifestyle.shop.data.model.Product
import com.offgridlifestyle.shop.data.model.ProductVariant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.cartDataStore by preferencesDataStore(name = "offgrid_cart")

/**
 * Local cart state, persisted to DataStore so it survives app restarts.
 * Deliberately simple in-memory + fire-and-forget-persist design: there is
 * no server-side cart yet since checkout doesn't process payment (see
 * CheckoutViewModel), so the source of truth lives entirely on-device.
 */
class CartRepository(
    private val context: Context,
    private val externalScope: CoroutineScope
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val cartKey = stringPreferencesKey("cart_items_json")

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    init {
        externalScope.launch {
            val stored = context.cartDataStore.data.first()[cartKey]
            if (!stored.isNullOrBlank()) {
                runCatching { json.decodeFromString<List<CartItem>>(stored) }
                    .onSuccess { _cartItems.value = it }
            }
        }
    }

    fun addItem(product: Product, variant: ProductVariant, quantity: Int = 1) {
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.variantId == variant.id }
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(
                CartItem(
                    variantId = variant.id,
                    productId = product.id,
                    productHandle = product.handle,
                    productTitle = product.title,
                    variantTitle = variant.title,
                    imageUrl = product.primaryImageUrl,
                    unitPrice = variant.price,
                    currencyCode = variant.currencyCode,
                    quantity = quantity
                )
            )
        }
        updateAndPersist(current)
    }

    fun updateQuantity(variantId: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(variantId)
            return
        }
        val current = _cartItems.value.map {
            if (it.variantId == variantId) it.copy(quantity = quantity) else it
        }
        updateAndPersist(current)
    }

    fun removeItem(variantId: String) {
        updateAndPersist(_cartItems.value.filterNot { it.variantId == variantId })
    }

    fun clear() {
        updateAndPersist(emptyList())
    }

    private fun updateAndPersist(items: List<CartItem>) {
        _cartItems.value = items
        externalScope.launch {
            context.cartDataStore.edit { prefs ->
                prefs[cartKey] = json.encodeToString(items)
            }
        }
    }
}
