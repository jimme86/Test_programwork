package com.offgridlifestyle.shop.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class GraphQLEnvelope<T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null
)

@Serializable
data class GraphQLError(val message: String)

@Serializable
data class ProductsData(val products: ProductConnection)

@Serializable
data class ProductByHandleData(val productByHandle: ProductNode?)

@Serializable
data class ProductConnection(
    val pageInfo: PageInfo,
    val edges: List<ProductEdge>
)

@Serializable
data class PageInfo(val hasNextPage: Boolean, val endCursor: String? = null)

@Serializable
data class ProductEdge(val cursor: String, val node: ProductNode)

@Serializable
data class ProductNode(
    val id: String,
    val handle: String,
    val title: String,
    val description: String,
    val productType: String,
    val availableForSale: Boolean,
    val images: ImageConnection,
    val priceRange: PriceRange,
    val variants: VariantConnection
)

@Serializable
data class ImageConnection(val edges: List<ImageEdge>)

@Serializable
data class ImageEdge(val node: ImageNode)

@Serializable
data class ImageNode(val url: String)

@Serializable
data class PriceRange(
    val minVariantPrice: Money,
    val maxVariantPrice: Money
)

@Serializable
data class Money(val amount: String, val currencyCode: String)

@Serializable
data class VariantConnection(val edges: List<VariantEdge>)

@Serializable
data class VariantEdge(val node: VariantNode)

@Serializable
data class VariantNode(
    val id: String,
    val title: String,
    val availableForSale: Boolean,
    val price: Money,
    val selectedOptions: List<SelectedOption> = emptyList()
)

@Serializable
data class SelectedOption(val name: String, val value: String)
