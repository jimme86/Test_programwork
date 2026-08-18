package com.offgridlifestyle.shop.ui.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offgridlifestyle.shop.BuildConfig
import com.offgridlifestyle.shop.ui.components.OffGridTopBar

@Composable
fun AboutScreen() {
    Scaffold(
        topBar = { OffGridTopBar(title = "About") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(text = "OffGrid Lifestyle", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Gear for living self-reliantly — solar power, water filtration, cooking, " +
                    "lighting, shelter, and tools built for people who spend real time off the grid.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            Text(text = "App version", style = MaterialTheme.typography.titleMedium)
            Text(
                text = BuildConfig.VERSION_NAME,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(text = "Contact", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 20.dp))
            Text(
                text = "For order questions, get in touch through the store's usual channels — " +
                    "checkout in this app collects your details so we can follow up directly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
