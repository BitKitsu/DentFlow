package com.example.dentflow_android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    // Stan pól formularza
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Stan błędów walidacji
    var showError by remember { mutableStateOf(false) }

    // Proste funkcje sprawdzające
    val isEmailValid = email.contains("@") && email.contains(".")
    val isPasswordValid = password.length >= 6
    val isPhoneValid = phone.length >= 9
    val areFieldsNotEmpty = firstname.isNotBlank() && lastname.isNotBlank()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.secondary,
        unfocusedTextColor = MaterialTheme.colorScheme.secondary,
        focusedLabelColor = MaterialTheme.colorScheme.secondary,
        unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.secondary,
        // Dodajemy kolory dla błędów
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorSupportingTextColor = MaterialTheme.colorScheme.error
    )

    val textFieldTextStyle = TextStyle(color = MaterialTheme.colorScheme.secondary)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Dołącz do DentFlow",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Imię
        OutlinedTextField(
            value = firstname,
            onValueChange = { firstname = it; showError = false },
            label = { Text("Imię") },
            isError = showError && firstname.isBlank(),
            supportingText = { if (showError && firstname.isBlank()) Text("Imię jest wymagane") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = textFieldTextStyle,
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Nazwisko
        OutlinedTextField(
            value = lastname,
            onValueChange = { lastname = it; showError = false },
            label = { Text("Nazwisko") },
            isError = showError && lastname.isBlank(),
            supportingText = { if (showError && lastname.isBlank()) Text("Nazwisko jest wymagane") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = textFieldTextStyle,
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Numer telefonu
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; showError = false },
            label = { Text("Numer telefonu") },
            isError = showError && !isPhoneValid,
            supportingText = { if (showError && !isPhoneValid) Text("Min. 9 cyfr") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = textFieldTextStyle,
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // E-mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; showError = false },
            label = { Text("E-mail") },
            isError = showError && !isEmailValid,
            supportingText = { if (showError && !isEmailValid) Text("Niepoprawny format e-mail") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = textFieldTextStyle,
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Hasło
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; showError = false },
            label = { Text("Hasło") },
            isError = showError && !isPasswordValid,
            supportingText = { if (showError && !isPasswordValid) Text("Hasło musi mieć min. 6 znaków") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = textFieldTextStyle,
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Przycisk Rejestracji
        Button(
            onClick = {
                if (areFieldsNotEmpty && isEmailValid && isPasswordValid && isPhoneValid) {
                    onRegisterSuccess()
                } else {
                    showError = true // Pokazuje błędy we wszystkich polach
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("ZAREJESTRUJ SIĘ", color = Color.White, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = { onBackToLogin() }) {
            Text(
                text = "Masz już konto? Zaloguj się",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}