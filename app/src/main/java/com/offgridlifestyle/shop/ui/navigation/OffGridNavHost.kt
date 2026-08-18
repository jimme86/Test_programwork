package com.offgridlifestyle.shop.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.offgridlifestyle.shop.AppContainer
import com.offgridlifestyle.shop.ui.about.AboutScreen
import com.offgridlifestyle.shop.ui.cart.CartScreen
import com.offgridlifestyle.shop.ui.cart.CartViewModel
import com.offgridlifestyle.shop.ui.catalog.CatalogScreen
import com.offgridlifestyle.shop.ui.catalog.CatalogViewModel
import com.offgridlifestyle.shop.ui.checkout.CheckoutScreen
import com.offgridlifestyle.shop.ui.checkout.CheckoutViewModel
import com.offgridlifestyle.shop.ui.checkout.OrderConfirmationScreen
import com.offgridlifestyle.shop.ui.home.HomeScreen
import com.offgridlifestyle.shop.ui.home.HomeViewModel
import com.offgridlifestyle.shop.ui.productdetail.ProductDetailScreen
import com.offgridlifestyle.shop.ui.productdetail.ProductDetailViewModel

private object Routes {
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val CATALOG_ARG = "category"
    const val PRODUCT_DETAIL = "product/{handle}"
    const val PRODUCT_DETAIL_ARG = "handle"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val ORDER_CONFIRMATION = "order_confirmation"
    const val ABOUT = "about"

    fun catalog(category: String? = null) =
        if (category != null) "$CATALOG?$CATALOG_ARG=$category" else CATALOG

    fun productDetail(handle: String) = "product/$handle"
}

private data class BottomDestination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomDestinations = listOf(
    BottomDestination(Routes.HOME, "Home", Icons.Filled.Home),
    BottomDestination(Routes.catalog(), "Shop", Icons.Filled.Storefront),
    BottomDestination(Routes.CART, "Cart", Icons.Filled.ShoppingCart),
    BottomDestination(Routes.ABOUT, "About", Icons.Filled.Info)
)

@Composable
fun OffGridNavHost(container: AppContainer) {
    val navController = rememberNavController()
    // Shared across Checkout + OrderConfirmation so the confirmed order survives the navigation.
    val checkoutViewModel = remember {
        CheckoutViewModel(container.cartRepository)
    }
    val cartItems by container.cartRepository.cartItems.collectAsState()
    val itemCount = cartItems.sumOf { it.quantity }

    Scaffold(
        bottomBar = {
            OffGridBottomBar(navController = navController, cartItemCount = itemCount)
        }
    ) { padding ->
        // Each destination hosts its own Scaffold + TopAppBar (which already handles the
        // status bar inset), so only the bottom bar's height needs to be reserved here.
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable(Routes.HOME) {
                val viewModel: HomeViewModel = viewModel(
                    factory = viewModelFactory { initializer { HomeViewModel(container.productRepository) } }
                )
                HomeScreen(
                    viewModel = viewModel,
                    cartItemCount = itemCount,
                    onProductClick = { handle -> navController.navigate(Routes.productDetail(handle)) },
                    onCategoryClick = { category -> navController.navigate(Routes.catalog(category)) },
                    onSeeAllClick = { navController.navigate(Routes.catalog()) },
                    onCartClick = { navController.navigate(Routes.CART) }
                )
            }

            composable(
                route = "${Routes.CATALOG}?${Routes.CATALOG_ARG}={${Routes.CATALOG_ARG}}",
                arguments = listOf(navArgument(Routes.CATALOG_ARG) {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString(Routes.CATALOG_ARG)
                val viewModel: CatalogViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { CatalogViewModel(container.productRepository, category) }
                    }
                )
                CatalogScreen(
                    viewModel = viewModel,
                    cartItemCount = itemCount,
                    onBack = { navController.popBackStack() },
                    onProductClick = { handle -> navController.navigate(Routes.productDetail(handle)) },
                    onCartClick = { navController.navigate(Routes.CART) }
                )
            }

            composable(
                route = Routes.PRODUCT_DETAIL,
                arguments = listOf(navArgument(Routes.PRODUCT_DETAIL_ARG) { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val handle = backStackEntry.arguments?.getString(Routes.PRODUCT_DETAIL_ARG).orEmpty()
                val viewModel: ProductDetailViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            ProductDetailViewModel(handle, container.productRepository, container.cartRepository)
                        }
                    }
                )
                ProductDetailScreen(
                    viewModel = viewModel,
                    cartItemCount = itemCount,
                    onBack = { navController.popBackStack() },
                    onCartClick = { navController.navigate(Routes.CART) }
                )
            }

            composable(Routes.CART) {
                val viewModel: CartViewModel = viewModel(
                    factory = viewModelFactory { initializer { CartViewModel(container.cartRepository) } }
                )
                CartScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate(Routes.CHECKOUT) }
                )
            }

            composable(Routes.CHECKOUT) {
                CheckoutScreen(
                    viewModel = checkoutViewModel,
                    onBack = { navController.popBackStack() },
                    onOrderSubmitted = {
                        navController.navigate(Routes.ORDER_CONFIRMATION) {
                            popUpTo(Routes.HOME)
                        }
                    }
                )
            }

            composable(Routes.ORDER_CONFIRMATION) {
                OrderConfirmationScreen(
                    viewModel = checkoutViewModel,
                    onContinueShopping = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.ABOUT) {
                AboutScreen()
            }
        }
    }
}

@Composable
private fun OffGridBottomBar(navController: NavHostController, cartItemCount: Int) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    NavigationBar {
        bottomDestinations.forEach { destination ->
            val selected = currentRoute?.hierarchy?.any {
                it.route == destination.route || (destination.route.startsWith(Routes.CATALOG) && it.route?.startsWith(Routes.CATALOG) == true)
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    if (destination.route == Routes.CART && cartItemCount > 0) {
                        BadgedBox(badge = { Badge { Text(cartItemCount.coerceAtMost(99).toString()) } }) {
                            Icon(destination.icon, contentDescription = destination.label)
                        }
                    } else {
                        Icon(destination.icon, contentDescription = destination.label)
                    }
                },
                label = { Text(destination.label) }
            )
        }
    }
}
