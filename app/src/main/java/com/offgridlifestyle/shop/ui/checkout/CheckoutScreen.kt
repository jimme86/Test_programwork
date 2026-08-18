package com.offgridlifestyle.shop.ui.checkout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.offgridlifestyle.shop.ui.components.OffGridTopBar

@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onOrderSubmitted: () -> Unit
) {
    val form by viewModel.formState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    LaunchedEffect(submitState) {
        if (submitState is CheckoutSubmitState.Submitted) onOrderSubmitted()
    }

    Scaffold(
        topBar = { OffGridTopBar(title = "Checkout", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "We'll use these details to confirm your order. No payment is collected in this preview build.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            LabeledField("Full name", form.fullName) { value ->
                viewModel.onFieldChanged { it.copy(fullName = value) }
            }
            LabeledField("Email", form.email, keyboardType = KeyboardType.Email) { value ->
                viewModel.onFieldChanged { it.copy(email = value) }
            }
            LabeledField("Phone (optional)", form.phone, keyboardType = KeyboardType.Phone) { value ->
                viewModel.onFieldChanged { it.copy(phone = value) }
            }
            LabeledField("Address", form.addressLine) { value ->
                viewModel.onFieldChanged { it.copy(addressLine = value) }
            }
            LabeledField("City", form.city) { value ->
                viewModel.onFieldChanged { it.copy(city = value) }
            }
            LabeledField("Postal code", form.postalCode) { value ->
                viewModel.onFieldChanged { it.copy(postalCode = value) }
            }
            LabeledField("Country", form.country) { value ->
                viewModel.onFieldChanged { it.copy(country = value) }
            }
            LabeledField("Order notes (optional)", form.notes) { value ->
                viewModel.onFieldChanged { it.copy(notes = value) }
            }

            Button(
                onClick = viewModel::submit,
                enabled = form.isValid && submitState !is CheckoutSubmitState.Submitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                if (submitState is CheckoutSubmitState.Submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit order request")
                }
            }
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    )
}
