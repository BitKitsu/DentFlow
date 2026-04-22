package com.example.dentflow_android.Screens

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.remote.RegisterRequest
import com.example.dentflow_android.ui.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") } // NOWE: Powtórz hasło

    val isLoading by viewModel.isLoading.collectAsState()
    val serverErrorMessage by viewModel.errorMessage.collectAsState()

    var showError by remember { mutableStateOf(false) }

    // Walidacja
    val isEmailValid = email.contains("@") && email.contains(".")
    val isPasswordValid = password.length >= 6
    val passwordsMatch = password == confirmPassword && password.isNotEmpty() // NOWE: Czy hasła są takie same
    val isPhoneValid = phone.length >= 9
    val areFieldsNotEmpty = firstname.isNotBlank() && lastname.isNotBlank()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.secondary,
        unfocusedTextColor = MaterialTheme.colorScheme.secondary,
        focusedLabelColor = MaterialTheme.colorScheme.secondary,
        unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.secondary,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error
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
            enabled = !isLoading,
            isError = showError && firstname.isBlank(),
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
            enabled = !isLoading,
            isError = showError && lastname.isBlank(),
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
            enabled = !isLoading,
            isError = showError && !isPhoneValid,
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
            enabled = !isLoading,
            isError = showError && !isEmailValid,
            modifier = Modifier.fillMaxWidth(),
            textStyle = textFieldTextStyle,
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Hasło (Z GWIAZDKAMI)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; showError = false },
            label = { Text("Hasło") },
            enabled = !isLoading,
            isError = showError && !isPasswordValid,
            visualTransformation = PasswordVisualTransformation(), // GWIAZDKI
            modifier = Modifier.fillMaxWidth(),
            textStyle = textFieldTextStyle,
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Powtórz hasło (NOWE POLE)
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; showError = false },
            label = { Text("Powtórz hasło") },
            enabled = !isLoading,
            isError = showError && !passwordsMatch,
            visualTransformation = PasswordVisualTransformation(), // GWIAZDKI
            modifier = Modifier.fillMaxWidth(),
            textStyle = textFieldTextStyle,
            colors = textFieldColors,
            supportingText = {
                if (showError && !passwordsMatch && confirmPassword.isNotEmpty()) {
                    Text("Hasła muszą być identyczne")
                }
            }
        )

        if (serverErrorMessage != null) {
            Text(
                text = serverErrorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (areFieldsNotEmpty && isEmailValid && isPasswordValid && isPhoneValid && passwordsMatch) {
                    val request = RegisterRequest(
                        firstName = firstname,
                        lastName = lastname,
                        email = email,
                        password = password,
                        phone = phone
                    )
                    viewModel.register(request, onRegisterSuccess)
                } else {
                    showError = true
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("ZAREJESTRUJ SIĘ", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        TextButton(onClick = { onBackToLogin() }, enabled = !isLoading) {
            Text(
                text = "Masz już konto? Zaloguj się",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
