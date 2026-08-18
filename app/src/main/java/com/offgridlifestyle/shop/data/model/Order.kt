package com.offgridlifestyle.shop.data.model

/** Order details captured at checkout. No payment is processed on-device yet;
 *  this is submitted to the store owner / a future checkout backend. */
data class OrderRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val addressLine: String,
    val city: String,
    val postalCode: String,
    val country: String,
    val notes: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val currencyCode: String
)
