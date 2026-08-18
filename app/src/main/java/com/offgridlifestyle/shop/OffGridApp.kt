package com.offgridlifestyle.shop

import android.app.Application
import com.offgridlifestyle.shop.data.cart.CartRepository
import com.offgridlifestyle.shop.data.remote.ShopifyConfig
import com.offgridlifestyle.shop.data.repository.MockProductRepository
import com.offgridlifestyle.shop.data.repository.ProductRepository
import com.offgridlifestyle.shop.data.repository.ShopifyProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/** Lightweight manual dependency container. The app is small enough that a
 *  full DI framework (Hilt/Koin) would add more ceremony than value; this
 *  container is the single place wiring is decided. */
class AppContainer(application: Application) {
    val applicationScope = CoroutineScope(SupervisorJob())

    val productRepository: ProductRepository =
        if (ShopifyConfig.isConfigured) ShopifyProductRepository() else MockProductRepository()

    val cartRepository = CartRepository(application, applicationScope)
}

class OffGridApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
