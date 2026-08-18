package com.offgridlifestyle.shop.ui.productdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.offgridlifestyle.shop.ui.components.EmptyState
import com.offgridlifestyle.shop.ui.components.OffGridTopBar
import com.offgridlifestyle.shop.util.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    cartItemCount: Int,
    onBack: () -> Unit,
    onCartClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.justAddedToCart) {
        if (uiState.justAddedToCart) {
            snackbarHostState.showSnackbar("Added to cart")
        }
    }

    Scaffold(
        topBar = {
            OffGridTopBar(
                title = uiState.product?.title ?: "Product",
                onBack = onBack,
                cartItemCount = cartItemCount,
                onCartClick = onCartClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.product == null -> EmptyState(
                icon = Icons.Filled.ErrorOutline,
                title = "Product not found",
                message = uiState.errorMessage ?: "This product may no longer be available.",
                modifier = Modifier.padding(padding)
            )

            else -> ProductDetailContent(
                uiState = uiState,
                onVariantSelected = viewModel::onVariantSelected,
                onQuantityChanged = viewModel::onQuantityChanged,
                onAddToCart = viewModel::addToCart,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ProductDetailContent(
    uiState: ProductDetailUiState,
    onVariantSelected: (com.offgridlifestyle.shop.data.model.ProductVariant) -> Unit,
    onQuantityChanged: (Int) -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val product = uiState.product ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (product.imageUrls.size > 1) {
            LazyRow {
                items(product.imageUrls) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(340.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        } else {
            AsyncImage(
                model = product.primaryImageUrl,
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = product.category, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(text = product.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 4.dp))
            val variant = uiState.selectedVariant
            Text(
                text = if (variant != null) formatPrice(variant.price, variant.currencyCode)
                else formatPrice(product.minPrice, product.currencyCode),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (product.variants.size > 1) {
                Text(text = "Options", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 20.dp))
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    product.variants.forEach { v ->
                        FilterChip(
                            selected = v.id == variant?.id,
                            onClick = { onVariantSelected(v) },
                            enabled = v.availableForSale,
                            label = { Text(v.title) }
                        )
                    }
                }
            }

            Text(text = "Description", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 20.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )

            QuantitySelector(
                quantity = uiState.quantity,
                onQuantityChanged = onQuantityChanged,
                modifier = Modifier.padding(top = 24.dp)
            )

            val canAdd = variant?.availableForSale == true
            Button(
                onClick = onAddToCart,
                enabled = canAdd,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                if (uiState.justAddedToCart) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Added to cart")
                } else {
                    Text(if (canAdd) "Add to cart" else "Out of stock")
                }
            }
        }
    }
}

@Composable
private fun QuantitySelector(quantity: Int, onQuantityChanged: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Quantity", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 16.dp))
        Card(shape = RoundedCornerShape(50), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onQuantityChanged(quantity - 1) }) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity")
                }
                Text(text = quantity.toString(), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { onQuantityChanged(quantity + 1) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase quantity")
                }
            }
        }
    }
}
