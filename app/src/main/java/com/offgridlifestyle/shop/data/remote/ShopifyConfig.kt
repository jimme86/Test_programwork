package com.offgridlifestyle.shop.data.remote

import com.offgridlifestyle.shop.BuildConfig

/**
 * Reads Shopify Storefront API credentials that were injected at build time
 * from local.properties (see local.properties.example). When either value
 * is blank the app falls back to the bundled demo catalog automatically.
 */
object ShopifyConfig {
    val shopDomain: String = BuildConfig.SHOPIFY_SHOP_DOMAIN
    val storefrontToken: String = BuildConfig.SHOPIFY_STOREFRONT_TOKEN

    val isConfigured: Boolean
        get() = shopDomain.isNotBlank() && storefrontToken.isNotBlank()

    /** e.g. https://offgrid-lifestyle.myshopify.com/api/2025-01/graphql.json */
    val storefrontApiUrl: String
        get() = "https://$shopDomain/api/2025-01/graphql.json"
}
