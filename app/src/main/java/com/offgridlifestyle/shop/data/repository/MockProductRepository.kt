package com.offgridlifestyle.shop.data.repository

import com.offgridlifestyle.shop.data.model.Product
import kotlinx.coroutines.delay

/** Serves the bundled sample catalog. Active whenever Shopify credentials
 *  are not configured, so the app is always usable out of the box. */
class MockProductRepository : ProductRepository {

    override val isLiveStore: Boolean = false

    override suspend fun getProducts(category: String?, searchQuery: String?): Result<List<Product>> {
        delay(250) // simulate network latency so loading states are visible
        var results = SampleProducts.all
        if (!category.isNullOrBlank()) {
            results = results.filter { it.category.equals(category, ignoreCase = true) }
        }
        if (!searchQuery.isNullOrBlank()) {
            val q = searchQuery.trim()
            results = results.filter {
                it.title.contains(q, ignoreCase = true) || it.description.contains(q, ignoreCase = true)
            }
        }
        return Result.success(results)
    }

    override suspend fun getProductByHandle(handle: String): Result<Product?> {
        delay(150)
        return Result.success(SampleProducts.all.find { it.handle == handle })
    }

    override suspend fun getCategories(): Result<List<String>> {
        return Result.success(SampleProducts.categories)
    }
}
