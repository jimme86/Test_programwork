package com.offgridlifestyle.shop.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.offgridlifestyle.shop.ui.components.OffGridTopBar
import com.offgridlifestyle.shop.ui.components.ProductCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    cartItemCount: Int,
    onProductClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            OffGridTopBar(
                title = "OffGrid Lifestyle",
                cartItemCount = cartItemCount,
                onCartClick = onCartClick
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        HomeContent(
            padding = padding,
            uiState = uiState,
            onProductClick = onProductClick,
            onCategoryClick = onCategoryClick,
            onSeeAllClick = onSeeAllClick
        )
    }
}

@Composable
private fun HomeContent(
    padding: PaddingValues,
    uiState: HomeUiState,
    onProductClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSeeAllClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { HeroBanner(isLiveStore = uiState.isLiveStore) }

        if (uiState.categories.isNotEmpty()) {
            item {
                Text(
                    text = "Shop by category",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.categories) { category ->
                        AssistChip(onClick = { onCategoryClick(category) }, label = { Text(category) })
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 12.dp)
            ) {
                Text(text = "Featured gear", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Tap “See all” to browse the full catalog",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { FeaturedProductsGrid(uiState = uiState, onProductClick = onProductClick) }

        item {
            Card(
                onClick = onSeeAllClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Browse the full catalog →",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/** Featured products laid out as a simple 2-column grid, nested inside the
 *  outer LazyColumn (kept non-lazy here since the list is short). */
@Composable
private fun FeaturedProductsGrid(uiState: HomeUiState, onProductClick: (String) -> Unit) {
    val rows = uiState.featuredProducts.chunked(2)
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        rows.forEach { rowProducts ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowProducts.forEach { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.handle) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowProducts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun HeroBanner(isLiveStore: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Live off the grid.",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "Solar power, water filtration, and rugged gear for a self-reliant life.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (!isLiveStore) {
                DemoModeBadge()
            }
        }
    }
}

@Composable
private fun DemoModeBadge() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
        Icon(
            imageVector = Icons.Filled.Cloud,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = "Demo Mode — showing sample products. Configure Shopify in local.properties to go live.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
