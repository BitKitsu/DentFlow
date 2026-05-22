package com.example.dentflow_android.Screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.remote.RegisterRequest
import com.example.dentflow_android.data.remote.*
import androidx.compose.foundation.background
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
    var confirmPassword by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val serverErrorMessage by viewModel.errorMessage.collectAsState()

    var showError by remember { mutableStateOf(false) }

    // Walidacja
    val isEmailValid = email.contains("@") && email.contains(".")
    val isPasswordValid = password.length >= 6
    val passwordsMatch = password == confirmPassword && password.isNotEmpty()
    val isPhoneValid = phone.length >= 9
    val areFieldsNotEmpty = firstname.isNotBlank() && lastname.isNotBlank()

    // Styl pól tekstowych
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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

        Text(
            text = "Zarządzaj swoją kliniką z łatwością",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Imię i Nazwisko w jednym rzędzie
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = firstname,
                onValueChange = { firstname = it; showError = false },
                label = { Text("Imię") },
                enabled = !isLoading,
                isError = showError && firstname.isBlank(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = lastname,
                onValueChange = { lastname = it; showError = false },
                label = { Text("Nazwisko") },
                enabled = !isLoading,
                isError = showError && lastname.isBlank(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Numer telefonu
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; showError = false },
            label = { Text("Numer telefonu") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            enabled = !isLoading,
            isError = showError && !isPhoneValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // E-mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; showError = false },
            label = { Text("E-mail") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            enabled = !isLoading,
            isError = showError && !isEmailValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Hasło
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; showError = false },
            label = { Text("Hasło (min. 6 znaków)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            enabled = !isLoading,
            isError = showError && !isPasswordValid,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Powtórz hasło
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; showError = false },
            label = { Text("Powtórz hasło") },
            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
            enabled = !isLoading,
            isError = showError && !passwordsMatch,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            singleLine = true,
            supportingText = {
                if (showError && !passwordsMatch && confirmPassword.isNotEmpty()) {
                    Text("Hasła muszą być identyczne", color = MaterialTheme.colorScheme.error)
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

        // Przycisk Rejestracji
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
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("ZAREJESTRUJ SIĘ", fontWeight = FontWeight.Bold)
            }
        }

        TextButton(onClick = { onBackToLogin() }, enabled = !isLoading) {
            Text(
                text = "Masz już konto? Zaloguj się",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}