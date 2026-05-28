package com.example.dentflow_android.Screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.remote.AuthViewModel

private val EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
private val PHONE_REGEX = Regex("^\\+?[0-9][\\s\\-]?([0-9][\\s\\-]?){8,14}$")
private val NAME_REGEX  = Regex("^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ\\s\\-]{2,50}$")

@Composable
fun AccountDataScreen(
    onBackClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("dentflow_prefs", android.content.Context.MODE_PRIVATE)
    }

    // Personal data
    var firstName      by remember { mutableStateOf(prefs.getString("user_first_name",  "") ?: "") }
    var lastName       by remember { mutableStateOf(prefs.getString("user_last_name",   "") ?: "") }
    var phone          by remember { mutableStateOf(prefs.getString("user_phone",        "") ?: "") }
    var email          by remember { mutableStateOf(prefs.getString("user_email",        "") ?: "") }

    // Address (structured, like location table)
    var addressStreet  by remember { mutableStateOf(prefs.getString("user_addr_street",  "") ?: "") }
    var addressCity    by remember { mutableStateOf(prefs.getString("user_addr_city",    "") ?: "") }
    var addressZip     by remember { mutableStateOf(prefs.getString("user_addr_zip",     "") ?: "") }
    var addressCountry by remember { mutableStateOf(prefs.getString("user_addr_country", "") ?: "") }

    // Password
    var currentPassword  by remember { mutableStateOf("") }
    var newPassword      by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var showCurrent      by remember { mutableStateOf(false) }
    var showNew          by remember { mutableStateOf(false) }
    var showConfirm      by remember { mutableStateOf(false) }

    // Feedback
    var profileError    by remember { mutableStateOf<String?>(null) }
    var profileSuccess  by remember { mutableStateOf<String?>(null) }
    var passwordError   by remember { mutableStateOf<String?>(null) }
    var passwordSuccess by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()

    // Validation
    val firstNameError = firstName.isNotBlank() && !NAME_REGEX.matches(firstName)
    val lastNameError  = lastName.isNotBlank()  && !NAME_REGEX.matches(lastName)
    val emailError     = email.isNotBlank()     && !EMAIL_REGEX.matches(email)
    val phoneError     = phone.isNotBlank()     && !PHONE_REGEX.matches(phone)

    val anyProfileField = firstName.isNotBlank() || lastName.isNotBlank() || email.isNotBlank()
            || phone.isNotBlank() || addressStreet.isNotBlank() || addressCity.isNotBlank()
            || addressZip.isNotBlank() || addressCountry.isNotBlank()
    val profileFormValid = !firstNameError && !lastNameError && !emailError && !phoneError
            && anyProfileField

    val sameAsOld      = currentPassword.isNotBlank() && newPassword == currentPassword
    val passwordsMatch = newPassword == confirmPassword
    val passwordFormValid = currentPassword.isNotBlank() && newPassword.length >= 8
            && !sameAsOld && passwordsMatch && confirmPassword.isNotBlank()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Dane konta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Personal data ────────────────────────────────────────────────
            SectionHeader(icon = Icons.Default.Person, title = "Dane osobowe")

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it; profileError = null; profileSuccess = null },
                    label = { Text("Imię") },
                    isError = firstNameError,
                    supportingText = {
                        if (firstNameError) Text("Min. 2 znaki, tylko litery",
                            color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it; profileError = null; profileSuccess = null },
                    label = { Text("Nazwisko") },
                    isError = lastNameError,
                    supportingText = {
                        if (lastNameError) Text("Min. 2 znaki, tylko litery",
                            color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; profileError = null; profileSuccess = null },
                label = { Text("Numer telefonu") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                isError = phoneError,
                supportingText = {
                    if (phoneError) Text("Np. +48 123 456 789", color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; profileError = null; profileSuccess = null },
                label = { Text("Adres e-mail") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                isError = emailError,
                supportingText = {
                    if (emailError) Text("Nieprawidłowy adres e-mail",
                        color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // ── Address ──────────────────────────────────────────────────────
            SectionHeader(icon = Icons.Default.Home, title = "Adres zamieszkania")

            OutlinedTextField(
                value = addressStreet,
                onValueChange = { addressStreet = it; profileError = null; profileSuccess = null },
                label = { Text("Ulica i numer") },
                leadingIcon = { Icon(Icons.Default.Place, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = addressCity,
                    onValueChange = { addressCity = it; profileError = null; profileSuccess = null },
                    label = { Text("Miasto") },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = addressZip,
                    onValueChange = { addressZip = it; profileError = null; profileSuccess = null },
                    label = { Text("Kod pocztowy") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            OutlinedTextField(
                value = addressCountry,
                onValueChange = { addressCountry = it; profileError = null; profileSuccess = null },
                label = { Text("Kraj") },
                leadingIcon = { Icon(Icons.Default.Flag, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Feedback
            AnimatedVisibility(visible = profileError != null) {
                FeedbackCard(message = profileError ?: "", isError = true)
            }
            AnimatedVisibility(visible = profileSuccess != null) {
                FeedbackCard(message = profileSuccess ?: "", isError = false)
            }

            Button(
                onClick = {
                    profileError   = null
                    profileSuccess = null
                    viewModel.updateProfile(
                        firstName      = firstName.takeIf      { it.isNotBlank() },
                        lastName       = lastName.takeIf       { it.isNotBlank() },
                        phone          = phone.takeIf          { it.isNotBlank() },
                        email          = email.takeIf          { it.isNotBlank() },
                        addressStreet  = addressStreet.takeIf  { it.isNotBlank() },
                        addressCity    = addressCity.takeIf    { it.isNotBlank() },
                        addressZip     = addressZip.takeIf     { it.isNotBlank() },
                        addressCountry = addressCountry.takeIf { it.isNotBlank() },
                        onSuccess = { profileSuccess = "Dane zostały zapisane." },
                        onError   = { profileError   = it }
                    )
                },
                enabled = profileFormValid && !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zapisz dane", fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // ── Password change ──────────────────────────────────────────────
            SectionHeader(icon = Icons.Default.Lock, title = "Zmiana hasła")

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it; passwordError = null; passwordSuccess = null },
                label = { Text("Obecne hasło") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showCurrent = !showCurrent }) {
                        Icon(if (showCurrent) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; passwordError = null; passwordSuccess = null },
                label = { Text("Nowe hasło") },
                leadingIcon = { Icon(Icons.Default.LockReset, null) },
                trailingIcon = {
                    IconButton(onClick = { showNew = !showNew }) {
                        Icon(if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = (newPassword.isNotBlank() && newPassword.length < 8) || sameAsOld,
                supportingText = {
                    when {
                        sameAsOld -> Text("Nowe hasło nie może być takie samo jak obecne",
                            color = MaterialTheme.colorScheme.error)
                        newPassword.isNotBlank() && newPassword.length < 8 ->
                            Text("Minimum 8 znaków", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; passwordError = null; passwordSuccess = null },
                label = { Text("Powtórz nowe hasło") },
                leadingIcon = { Icon(Icons.Default.LockOpen, null) },
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = confirmPassword.isNotBlank() && !passwordsMatch,
                supportingText = {
                    if (confirmPassword.isNotBlank() && !passwordsMatch)
                        Text("Hasła nie są identyczne", color = MaterialTheme.colorScheme.error)
                },
                singleLine = true
            )

            AnimatedVisibility(visible = passwordError != null) {
                FeedbackCard(message = passwordError ?: "", isError = true)
            }
            AnimatedVisibility(visible = passwordSuccess != null) {
                FeedbackCard(message = passwordSuccess ?: "", isError = false)
            }

            Button(
                onClick = {
                    passwordError   = null
                    passwordSuccess = null
                    viewModel.changePassword(
                        currentPassword = currentPassword,
                        newPassword     = newPassword,
                        onSuccess = {
                            passwordSuccess = "Hasło zostało zmienione."
                            currentPassword = ""; newPassword = ""; confirmPassword = ""
                        },
                        onError = { passwordError = it }
                    )
                },
                enabled = passwordFormValid && !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Key, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zmień hasło", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun FeedbackCard(message: String, isError: Boolean) {
    val bg   = if (isError) MaterialTheme.colorScheme.errorContainer   else MaterialTheme.colorScheme.primaryContainer
    val fg   = if (isError) MaterialTheme.colorScheme.onErrorContainer  else MaterialTheme.colorScheme.onPrimaryContainer
    val icon = if (isError) Icons.Default.Error else Icons.Default.CheckCircle
    val tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Card(colors = CardDefaults.cardColors(containerColor = bg), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(message, color = fg, style = MaterialTheme.typography.bodySmall)
        }
    }
}
