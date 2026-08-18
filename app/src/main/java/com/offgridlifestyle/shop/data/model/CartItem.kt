package com.offgridlifestyle.shop.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val variantId: String,
    val productId: String,
    val productHandle: String,
    val productTitle: String,
    val variantTitle: String,
    val imageUrl: String?,
    val unitPrice: Double,
    val currencyCode: String,
    val quantity: Int
) {
    val lineTotal: Double get() = unitPrice * quantity
}
