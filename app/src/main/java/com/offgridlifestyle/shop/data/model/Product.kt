package com.offgridlifestyle.shop.data.model

import java.math.BigDecimal

/**
 * Domain model for a product, mapped from either the Shopify Storefront API
 * or the built-in sample catalog. UI code only ever depends on this type,
 * never on the remote DTOs.
 */
data class Product(
    val id: String,
    val handle: String,
    val title: String,
    val description: String,
    val category: String,
    val imageUrls: List<String>,
    val currencyCode: String,
    val priceRange: ClosedFloatingPointRange<Double>,
    val variants: List<ProductVariant>,
    val availableForSale: Boolean
) {
    val primaryImageUrl: String? get() = imageUrls.firstOrNull()
    val minPrice: Double get() = priceRange.start
}

data class ProductVariant(
    val id: String,
    val title: String,
    val price: Double,
    val currencyCode: String,
    val availableForSale: Boolean,
    val selectedOptions: Map<String, String> = emptyMap()
)

/** Rounds a raw price to 2 decimals for display/math without float drift. */
fun Double.toMoney(): BigDecimal = BigDecimal.valueOf(this).setScale(2, java.math.RoundingMode.HALF_UP)
