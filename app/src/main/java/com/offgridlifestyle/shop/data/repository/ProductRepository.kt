package com.offgridlifestyle.shop.data.repository

import com.offgridlifestyle.shop.data.model.Product

interface ProductRepository {
    /** True when this repository is talking to a live Shopify store rather
     *  than the bundled sample catalog. Used to show a "Demo Mode" banner. */
    val isLiveStore: Boolean

    suspend fun getProducts(category: String? = null, searchQuery: String? = null): Result<List<Product>>
    suspend fun getProductByHandle(handle: String): Result<Product?>
    suspend fun getCategories(): Result<List<String>>
}
