package com.offgridlifestyle.shop.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Formats a raw amount + ISO currency code (e.g. "EUR") as a localized
 *  price string (e.g. "€219.00"), falling back gracefully for unknown codes. */
fun formatPrice(amount: Double, currencyCode: String): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        format.currency = Currency.getInstance(currencyCode)
        format.format(amount)
    } catch (e: IllegalArgumentException) {
        "%.2f %s".format(amount, currencyCode)
    }
}

fun formatPriceRange(min: Double, max: Double, currencyCode: String): String {
    return if (min == max) {
        formatPrice(min, currencyCode)
    } else {
        "${formatPrice(min, currencyCode)} – ${formatPrice(max, currencyCode)}"
    }
}
