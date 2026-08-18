package com.offgridlifestyle.shop.data.remote

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Builds a GraphQL `variables` map with correctly-typed JSON values —
 * important because Shopify's schema declares typed variables (e.g.
 * `$first: Int!`), and sending a JSON string where a number is expected
 * fails GraphQL variable coercion.
 */
fun graphQLVariables(vararg pairs: Pair<String, Any?>): Map<String, JsonElement> =
    pairs.associate { (key, value) -> key to value.toJsonElement() }

private fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Int -> JsonPrimitive(this)
    is Long -> JsonPrimitive(this)
    is Double -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    else -> JsonPrimitive(this.toString())
}
