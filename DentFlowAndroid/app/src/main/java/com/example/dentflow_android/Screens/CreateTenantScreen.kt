package com.example.dentflow_android.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dentflow_android.data.ViewModel.TenantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTenantScreen(
    onBack: () -> Unit,
    tenantViewModel: TenantViewModel
) {
    var name by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Polska") }

    var showErrors by remember { mutableStateOf(false) }

    val isLoading by tenantViewModel.isLoading
    val tenantState by tenantViewModel.tenantState

    LaunchedEffect(tenantState) {
        if (tenantState != null) {
            onBack()
        }
    }

    val isFormValid = name.isNotBlank() &&
            street.isNotBlank() &&
            city.isNotBlank() &&
            zip.length == 6

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zarejestruj Klinikę") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Dane podstawowe", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nazwa kliniki") },
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && name.isBlank()
            )

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Nazwa placówki (np. Gabinet Główny)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Adres", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = street,
                onValueChange = { street = it },
                label = { Text("Ulica i numer") },
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && street.isBlank()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = zip,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 5) {
                            zip = when {
                                digits.length > 2 -> "${digits.take(2)}-${digits.drop(2)}"
                                else -> digits
                            }
                        }
                    },
                    label = { Text("Kod pocztowy") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showErrors && zip.length < 6,
                    placeholder = { Text("00-000") }
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Miasto") },
                    modifier = Modifier.weight(2f),
                    isError = showErrors && city.isBlank()
                )
            }

            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("Kraj") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        tenantViewModel.registerClinic(
                            name = name,
                            locationName = if(locationName.isBlank()) "Placówka Główna" else locationName,
                            street = street,
                            city = city,
                            zip = zip,
                            country = country
                        )
                    } else {
                        showErrors = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Utwórz i zarejestruj")
                }
            }
        }
    }
}