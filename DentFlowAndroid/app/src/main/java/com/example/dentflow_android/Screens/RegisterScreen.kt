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
    var addressStreet by remember { mutableStateOf("") }
    var addressCity   by remember { mutableStateOf("") }
    var addressZip    by remember { mutableStateOf("") }
    var addressCountry by remember { mutableStateOf("") }

    val isLoading         by viewModel.isLoading.collectAsState()
    val serverErrorMessage by viewModel.errorMessage.collectAsState()

    var showError by remember { mutableStateOf(false) }

    val isFirstNameValid = REG_NAME_REGEX.matches(firstname)
    val isLastNameValid  = REG_NAME_REGEX.matches(lastname)
    val isEmailValid     = REG_EMAIL_REGEX.matches(email)
    val isPhoneValid     = REG_PHONE_REGEX.matches(phone)
    val isZipValid       = Regex("^[0-9]{5}$").matches(addressZip)
    val isStreetValid    = addressStreet.length >= 3
    val isCityValid      = addressCity.length >= 2
    val isCountryValid   = addressCountry.length >= 2
    val isPasswordValid  = password.length >= 8
    val passwordsMatch   = password == confirmPassword && password.isNotEmpty()

    val firstNameError = (showError && firstname.isBlank()) || (firstname.isNotBlank() && !isFirstNameValid)
    val lastNameError  = (showError && lastname.isBlank())  || (lastname.isNotBlank()  && !isLastNameValid)
    val emailError     = (showError && email.isBlank())     || (email.isNotBlank()     && !isEmailValid)
    val phoneError     = (showError && phone.isBlank())     || (phone.isNotBlank()     && !isPhoneValid)
    val zipError       = (showError && addressZip.isBlank()) || (addressZip.isNotBlank() && !isZipValid)
    val streetError    = (showError && addressStreet.isBlank()) || (addressStreet.isNotBlank() && !isStreetValid)
    val cityError      = (showError && addressCity.isBlank()) || (addressCity.isNotBlank() && !isCityValid)
    val countryError   = (showError && addressCountry.isBlank()) || (addressCountry.isNotBlank() && !isCountryValid)
    val passwordError  = (showError && password.isBlank())  || (password.isNotBlank()  && !isPasswordValid)
    val confirmError   = (showError && confirmPassword.isBlank()) || (confirmPassword.isNotBlank() && !passwordsMatch)

    val canSubmit = isFirstNameValid && isLastNameValid && isEmailValid
            && isPhoneValid && isZipValid && isStreetValid && isCityValid && isCountryValid 
            && isPasswordValid && passwordsMatch

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
                isError = firstNameError,
                supportingText = {
                    if (firstNameError)
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
                isError = lastNameError,
                supportingText = {
                    if (lastNameError)
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
            isError = phoneError,
            supportingText = {
                if (phoneError)
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
            isError = emailError,
            supportingText = {
                if (emailError)
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
            value = addressStreet,
            onValueChange = { addressStreet = it; showError = false },
            label = { Text("Ulica i numer") },
            leadingIcon = { Icon(Icons.Default.Place, null) },
            enabled = !isLoading,
            isError = streetError,
            supportingText = {
                if (streetError) Text("Min. 3 znaki", color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = addressCity,
                onValueChange = { addressCity = it; showError = false },
                label = { Text("Miasto") },
                enabled = !isLoading,
                isError = cityError,
                supportingText = {
                    if (cityError) Text("Min. 2 znaki", color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = addressZip,
                onValueChange = { input -> 
                    addressZip = input.filter { it.isDigit() }.take(5)
                    showError = false 
                },
                visualTransformation = { text ->
                    val trimmed = if (text.text.length >= 5) text.text.substring(0..4) else text.text
                    var out = ""
                    for (i in trimmed.indices) {
                        out += trimmed[i]
                        if (i == 1) out += "-"
                    }
                    val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
                        override fun originalToTransformed(offset: Int): Int {
                            if (offset <= 1) return offset
                            if (offset <= 5) return offset + 1
                            return 6
                        }
                        override fun transformedToOriginal(offset: Int): Int {
                            if (offset <= 2) return offset
                            if (offset <= 6) return offset - 1
                            return 5
                        }
                    }
                    androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(out), offsetMapping)
                },
                label = { 
                    Text(
                        text = "Kod pocztowy",
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                enabled = !isLoading,
                isError = zipError,
                supportingText = {
                    if (zipError) Text("00-000", color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        OutlinedTextField(
            value = addressCountry,
            onValueChange = { addressCountry = it; showError = false },
            label = { Text("Kraj") },
            leadingIcon = { Icon(Icons.Default.Flag, null) },
            enabled = !isLoading,
            isError = countryError,
            supportingText = {
                if (countryError) Text("Min. 2 znaki", color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; showError = false },
            label = { Text("Hasło (min. 8 znaków)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            enabled = !isLoading,
            isError = passwordError,
            supportingText = {
                if (passwordError)
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
            isError = confirmError,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            singleLine = true,
            supportingText = {
                if (confirmError)
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
                            firstName      = firstname,
                            lastName       = lastname,
                            email          = email,
                            password       = password,
                            phone          = phone,
                            addressStreet  = addressStreet,
                            addressCity    = addressCity,
                            addressZip     = if (addressZip.length == 5) "${addressZip.take(2)}-${addressZip.drop(2)}" else addressZip,
                            addressCountry = addressCountry
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