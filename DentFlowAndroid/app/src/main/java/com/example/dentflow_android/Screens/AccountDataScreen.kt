package com.example.dentflow_android.Screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.remote.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDataScreen(
    onBackClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("dentflow_prefs", android.content.Context.MODE_PRIVATE) }
    val userEmail = remember { prefs.getString("user_email", "") ?: "" }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrent by remember { mutableStateOf(false) }
    var showNew     by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage   by remember { mutableStateOf<String?>(null) }

    val isLoading by authViewModel.isLoading.collectAsState()

    val isFormValid = currentPassword.isNotBlank()
            && newPassword.length >= 8
            && newPassword == confirmPassword

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dane konta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Informacje o koncie
            Text(
                text = "Informacje o koncie",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Adres e-mail", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(userEmail.ifBlank { "Brak danych" }, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Zmiana hasła
            Text(
                text = "Zmiana hasła",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it; errorMessage = null },
                label = { Text("Obecne hasło") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showCurrent = !showCurrent }) {
                        Icon(if (showCurrent) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null)
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
                onValueChange = { newPassword = it; errorMessage = null },
                label = { Text("Nowe hasło (min. 8 znaków)") },
                leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showNew = !showNew }) {
                        Icon(if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null)
                    }
                },
                visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = newPassword.isNotBlank() && newPassword.length < 8,
                singleLine = true
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                label = { Text("Powtórz nowe hasło") },
                leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null)
                    }
                },
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = confirmPassword.isNotBlank() && newPassword != confirmPassword,
                supportingText = {
                    if (confirmPassword.isNotBlank() && newPassword != confirmPassword)
                        Text("Hasła nie są identyczne", color = MaterialTheme.colorScheme.error)
                },
                singleLine = true
            )

            // Komunikaty
            AnimatedVisibility(visible = errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMessage ?: "", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            AnimatedVisibility(visible = successMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(successMessage ?: "", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            Button(
                onClick = {
                    errorMessage = null
                    successMessage = null
                    authViewModel.changePassword(
                        currentPassword = currentPassword,
                        newPassword = newPassword,
                        onSuccess = {
                            successMessage = "Hasło zostało zmienione pomyślnie!"
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        },
                        onError = { msg -> errorMessage = msg }
                    )
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ZMIEŃ HASŁO", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
