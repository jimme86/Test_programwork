package com.offgridlifestyle.shop.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class ShopifyResult<out T> {
    data class Success<T>(val data: T) : ShopifyResult<T>()
    data class Failure(val message: String, val cause: Throwable? = null) : ShopifyResult<Nothing>()
}

/** Thin, dependency-light GraphQL client for the Shopify Storefront API.
 *  Uses plain OkHttp + kotlinx.serialization instead of Apollo so the app
 *  builds without needing network access to a live store's schema at
 *  build time. */
class ShopifyGraphQLClient(
    private val config: ShopifyConfig = ShopifyConfig
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend inline fun <reified T> execute(
        query: String,
        variables: Map<String, JsonElement> = emptyMap()
    ): ShopifyResult<T> = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            return@withContext ShopifyResult.Failure("Shopify is not configured")
        }
        try {
            val body = json.encodeToString(
                GraphQLRequest.serializer(),
                GraphQLRequest(query = query, variables = variables)
            )
            val request = Request.Builder()
                .url(config.storefrontApiUrl)
                .addHeader("X-Shopify-Storefront-Access-Token", config.storefrontToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .post(body.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext ShopifyResult.Failure(
                        "Shopify request failed: HTTP ${response.code}"
                    )
                }
                val envelope = json.decodeFromString(
                    GraphQLEnvelope.serializer(serializer<T>()),
                    responseBody
                )
                val errors = envelope.errors
                if (!errors.isNullOrEmpty()) {
                    return@withContext ShopifyResult.Failure(errors.joinToString { it.message })
                }
                val data = envelope.data
                    ?: return@withContext ShopifyResult.Failure("Shopify returned no data")
                ShopifyResult.Success(data)
            }
        } catch (io: IOException) {
            ShopifyResult.Failure("Network error: ${io.message}", io)
        } catch (e: Exception) {
            ShopifyResult.Failure("Unexpected error: ${e.message}", e)
        }
    }
}
