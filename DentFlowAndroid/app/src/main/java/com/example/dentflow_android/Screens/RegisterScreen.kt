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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.remote.RegisterRequest
import com.example.dentflow_android.data.remote.*
import androidx.compose.foundation.background

private val REG_EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
private val REG_PHONE_REGEX = Regex("^\\+?[0-9][\\s\\-]?([0-9][\\s\\-]?){8,14}$")
private val REG_NAME_REGEX  = Regex("^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ\\s\\-]{2,50}$")

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var firstname by remember { mutableStateOf("") }
    var lastname  by remember { mutableStateOf("") }
    var phone     by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val isLoading         by viewModel.isLoading.collectAsState()
    val serverErrorMessage by viewModel.errorMessage.collectAsState()

    var showError by remember { mutableStateOf(false) }

    val isFirstNameValid = REG_NAME_REGEX.matches(firstname)
    val isLastNameValid  = REG_NAME_REGEX.matches(lastname)
    val isEmailValid     = REG_EMAIL_REGEX.matches(email)
    val isPhoneValid     = REG_PHONE_REGEX.matches(phone)
    val isPasswordValid  = password.length >= 8
    val passwordsMatch   = password == confirmPassword && password.isNotEmpty()

    val canSubmit = isFirstNameValid && isLastNameValid && isEmailValid
            && isPhoneValid && isPasswordValid && passwordsMatch

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor          = MaterialTheme.colorScheme.primary
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

        Text("Dołącz do DentFlow",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold)

        Text("Zarządzaj swoją kliniką z łatwością",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(32.dp))

        // Imię i Nazwisko
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = firstname,
                onValueChange = { firstname = it; showError = false },
                label = { Text("Imię") },
                enabled = !isLoading,
                isError = showError && !isFirstNameValid,
                supportingText = {
                    if (showError && !isFirstNameValid)
                        Text("Min. 2 znaki, tylko litery",
                            color = MaterialTheme.colorScheme.error)
                },
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
                isError = showError && !isLastNameValid,
                supportingText = {
                    if (showError && !isLastNameValid)
                        Text("Min. 2 znaki, tylko litery",
                            color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; showError = false },
            label = { Text("Numer telefonu") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            enabled = !isLoading,
            isError = showError && !isPhoneValid,
            supportingText = {
                if (showError && !isPhoneValid)
                    Text("Nieprawidłowy numer (np. +48 123 456 789)",
                        color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; showError = false },
            label = { Text("E-mail") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            enabled = !isLoading,
            isError = showError && !isEmailValid,
            supportingText = {
                if (showError && !isEmailValid)
                    Text("Nieprawidłowy adres e-mail (np. jan@example.com)",
                        color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; showError = false },
            label = { Text("Hasło (min. 8 znaków)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            enabled = !isLoading,
            isError = showError && !isPasswordValid,
            supportingText = {
                if (showError && !isPasswordValid)
                    Text("Hasło musi mieć co najmniej 8 znaków",
                        color = MaterialTheme.colorScheme.error)
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

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
                if (showError && !passwordsMatch && confirmPassword.isNotEmpty())
                    Text("Hasła muszą być identyczne",
                        color = MaterialTheme.colorScheme.error)
            }
        )

        if (serverErrorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = serverErrorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                if (canSubmit) {
                    viewModel.register(
                        RegisterRequest(
                            firstName = firstname,
                            lastName  = lastname,
                            email     = email,
                            password  = password,
                            phone     = phone
                        ),
                        onRegisterSuccess
                    )
                } else {
                    showError = true
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White,
                    modifier = Modifier.size(24.dp))
            } else {
                Text("ZAREJESTRUJ SIĘ", fontWeight = FontWeight.Bold)
            }
        }

        TextButton(onClick = { onBackToLogin() }, enabled = !isLoading) {
            Text("Masz już konto? Zaloguj się",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}