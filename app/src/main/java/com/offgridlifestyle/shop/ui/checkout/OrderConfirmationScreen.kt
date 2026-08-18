package com.offgridlifestyle.shop.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.offgridlifestyle.shop.util.formatPrice

@Composable
fun OrderConfirmationScreen(
    viewModel: CheckoutViewModel,
    onContinueShopping: () -> Unit
) {
    val submitState by viewModel.submitState.collectAsState()
    val order = (submitState as? CheckoutSubmitState.Submitted)?.order

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = "Order request received!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Thanks${if (order != null) ", ${order.fullName}" else ""}. We'll reach out at " +
                    "${order?.email ?: "your email"} to confirm shipping and payment.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (order != null) {
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Order summary", style = MaterialTheme.typography.titleMedium)
                        order.items.forEach { item ->
                            Text(
                                text = "${item.quantity} × ${item.productTitle}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        Text(
                            text = "Total: ${formatPrice(order.subtotal, order.currencyCode)}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }

            Button(onClick = onContinueShopping, modifier = Modifier.padding(top = 32.dp)) {
                Text("Continue shopping")
            }
        }
    }
}
