package com.example.dentflow_android.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CreateBusinessScreen(onBusinessCreated: () -> Unit) {
    var clinicName by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var house_number by remember { mutableStateOf("") }
    var apartment_number by remember { mutableStateOf("") }
    var postal_code by remember { mutableStateOf("") }
    var nip by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var showErrors by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isFormValid = clinicName.isNotBlank() &&
            city.isNotBlank() &&
            nip.length == 10 &&
            phone.length >= 9 &&
            postal_code.length == 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AddBusiness,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Zarejestruj swoją firmę",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        BusinessInputField(
            value = clinicName,
            onValueChange = { clinicName = it },
            label = "Nazwa Kliniki / Gabinetu",
            icon = Icons.Default.Storefront,
            isError = showErrors && clinicName.isBlank(),
            errorText = "Nazwa jest wymagana"
        )

        BusinessInputField(
            value = street,
            onValueChange = { street = it },
            label = "Ulica",
            isError = showErrors && street.isBlank(),
            icon = Icons.Default.EditRoad
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                BusinessInputField(
                    value = house_number,
                    onValueChange = { house_number = it },
                    label = "Nr domu",
                    isError = showErrors && house_number.isBlank(),
                    icon = Icons.Default.Home
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                BusinessInputField(
                    value = apartment_number,
                    onValueChange = { apartment_number = it },
                    label = "Nr lokalu",
                    icon = Icons.Default.DoorFront
                )
            }
        }

        BusinessInputField(
            value = city,
            onValueChange = { city = it },
            label = "Miasto",
            icon = Icons.Default.LocationCity,
            isError = showErrors && city.isBlank(),
            errorText = "Podaj miasto"
        )

        // --- KOD POCZTOWY Z AUTOMATYCZNĄ KRESKĄ ---
        BusinessInputField(
            value = postal_code,
            onValueChange = { input ->
                val digits = input.filter { it.isDigit() }
                if (digits.length <= 5) {
                    postal_code = if (digits.length > 2) {
                        "${digits.take(2)}-${digits.drop(2)}"
                    } else {
                        digits
                    }
                }
            },
            label = "Kod pocztowy",
            icon = Icons.Default.MarkunreadMailbox,
            keyboardType = KeyboardType.Number,
            isError = showErrors && postal_code.length < 6,
            errorText = "Format: 00-000"
        )

        BusinessInputField(
            value = nip,
            onValueChange = { if (it.length <= 10) nip = it.filter { char -> char.isDigit() } },
            label = "NIP Firmy",
            icon = Icons.Default.Badge,
            keyboardType = KeyboardType.Number,
            isError = showErrors && nip.length != 10,
            errorText = "NIP musi mieć 10 cyfr"
        )

        BusinessInputField(
            value = phone,
            onValueChange = { if (it.length <= 12) phone = it.filter { char -> char.isDigit() } },
            label = "Telefon kontaktowy",
            icon = Icons.Default.Call,
            keyboardType = KeyboardType.Phone,
            isError = showErrors && phone.length < 9,
            errorText = "Błędny numer telefonu"
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (isFormValid) {
                    isLoading = true
                    scope.launch {
                        delay(1500)
                        isLoading = false
                        onBusinessCreated()
                    }
                } else {
                    showErrors = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("UTWÓRZ PROFIL FIRMY", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BusinessInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isError: Boolean = false,
    errorText: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(text = errorText, color = MaterialTheme.colorScheme.error)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.secondary,
            unfocusedTextColor = MaterialTheme.colorScheme.secondary,
            cursorColor = MaterialTheme.colorScheme.secondary,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error
        )
    )
}
