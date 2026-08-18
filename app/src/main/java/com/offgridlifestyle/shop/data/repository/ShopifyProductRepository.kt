package com.offgridlifestyle.shop.data.repository

import com.offgridlifestyle.shop.data.model.Product
import com.offgridlifestyle.shop.data.model.ProductVariant
import com.offgridlifestyle.shop.data.remote.GraphQLQueries
import com.offgridlifestyle.shop.data.remote.graphQLVariables
import com.offgridlifestyle.shop.data.remote.ProductByHandleData
import com.offgridlifestyle.shop.data.remote.ProductNode
import com.offgridlifestyle.shop.data.remote.ProductsData
import com.offgridlifestyle.shop.data.remote.ShopifyGraphQLClient
import com.offgridlifestyle.shop.data.remote.ShopifyResult

/** Talks to the live Shopify Storefront API (GraphQL). Active once
 *  SHOPIFY_SHOP_DOMAIN and SHOPIFY_STOREFRONT_TOKEN are set in local.properties. */
class ShopifyProductRepository(
    private val client: ShopifyGraphQLClient = ShopifyGraphQLClient()
) : ProductRepository {

    override val isLiveStore: Boolean = true

    override suspend fun getProducts(category: String?, searchQuery: String?): Result<List<Product>> {
        val searchTerms = buildList {
            if (!category.isNullOrBlank()) add("product_type:'${category.replace("'", "")}'")
            if (!searchQuery.isNullOrBlank()) add("title:*${searchQuery.trim()}*")
        }
        val queryFilter = searchTerms.joinToString(" AND ").ifBlank { null }

        return when (
            val result = client.execute<ProductsData>(
                query = GraphQLQueries.PRODUCTS_QUERY,
                variables = graphQLVariables(
                    "first" to 50,
                    "after" to null,
                    "query" to queryFilter
                )
            )
        ) {
            is ShopifyResult.Success -> Result.success(
                result.data.products.edges.map { it.node.toProduct() }
            )
            is ShopifyResult.Failure -> Result.failure(IllegalStateException(result.message, result.cause))
        }
    }

    override suspend fun getProductByHandle(handle: String): Result<Product?> {
        return when (
            val result = client.execute<ProductByHandleData>(
                query = GraphQLQueries.PRODUCT_BY_HANDLE_QUERY,
                variables = graphQLVariables("handle" to handle)
            )
        ) {
            is ShopifyResult.Success -> Result.success(result.data.productByHandle?.toProduct())
            is ShopifyResult.Failure -> Result.failure(IllegalStateException(result.message, result.cause))
        }
    }

    override suspend fun getCategories(): Result<List<String>> {
        // The Storefront API has no dedicated "distinct product types" query,
        // so derive categories from a broad product fetch.
        return getProducts().map { products ->
            products.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
        }
    }
}

private fun ProductNode.toProduct(): Product {
    val variants = variants.edges.map { edge ->
        val node = edge.node
        ProductVariant(
            id = node.id,
            title = node.title,
            price = node.price.amount.toDoubleOrNull() ?: 0.0,
            currencyCode = node.price.currencyCode,
            availableForSale = node.availableForSale,
            selectedOptions = node.selectedOptions.associate { it.name to it.value }
        )
    }
    val minPrice = priceRange.minVariantPrice.amount.toDoubleOrNull() ?: 0.0
    val maxPrice = priceRange.maxVariantPrice.amount.toDoubleOrNull() ?: minPrice
    return Product(
        id = id,
        handle = handle,
        title = title,
        description = description,
        category = productType,
        imageUrls = images.edges.map { it.node.url },
        currencyCode = priceRange.minVariantPrice.currencyCode,
        priceRange = minPrice..(maxOf(minPrice, maxPrice)),
        variants = variants,
        availableForSale = availableForSale
    )
}
