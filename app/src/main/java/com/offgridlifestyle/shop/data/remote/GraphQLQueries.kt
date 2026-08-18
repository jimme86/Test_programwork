package com.offgridlifestyle.shop.data.remote

/** Hand-written GraphQL documents for the Shopify Storefront API (2025-01).
 *  Kept minimal on purpose: only the fields the app's UI actually renders. */
object GraphQLQueries {

    private const val PRODUCT_FIELDS = """
        id
        handle
        title
        description
        productType
        availableForSale
        images(first: 8) {
            edges { node { url } }
        }
        priceRange {
            minVariantPrice { amount currencyCode }
            maxVariantPrice { amount currencyCode }
        }
        variants(first: 25) {
            edges {
                node {
                    id
                    title
                    availableForSale
                    price { amount currencyCode }
                    selectedOptions { name value }
                }
            }
        }
    """

    val PRODUCTS_QUERY = """
        query Products(${'$'}first: Int!, ${'$'}after: String, ${'$'}query: String) {
            products(first: ${'$'}first, after: ${'$'}after, query: ${'$'}query) {
                pageInfo { hasNextPage endCursor }
                edges {
                    cursor
                    node { $PRODUCT_FIELDS }
                }
            }
        }
    """.trimIndent()

    val PRODUCT_BY_HANDLE_QUERY = """
        query ProductByHandle(${'$'}handle: String!) {
            productByHandle(handle: ${'$'}handle) { $PRODUCT_FIELDS }
        }
    """.trimIndent()

    val COLLECTIONS_QUERY = """
        query Collections(${'$'}first: Int!) {
            collections(first: ${'$'}first) {
                edges { node { id handle title } }
            }
        }
    """.trimIndent()
}
