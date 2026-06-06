package pl.edu.ur.dentflow.Screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import pl.edu.ur.dentflow.data.ViewModel.FileViewModel
import pl.edu.ur.dentflow.data.remote.AuthViewModel

private val EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
private val PHONE_REGEX = Regex("^\\+?[0-9][\\s\\-]?([0-9][\\s\\-]?){8,14}$")
private val NAME_REGEX  = Regex("^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ\\s\\-]{2,50}$")

@Composable
fun AccountDataScreen(
    onBackClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    fileViewModel: FileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionState by viewModel.sessionState.collectAsState()

    // Personal data
    var firstName      by remember { mutableStateOf(sessionState.firstName) }
    var lastName       by remember { mutableStateOf(sessionState.lastName) }
    var phone          by remember { mutableStateOf(sessionState.phone) }
    var email          by remember { mutableStateOf(sessionState.email) }

    // Address (structured, like location table)
    var addressStreet  by remember { mutableStateOf(sessionState.addressStreet) }
    var addressCity    by remember { mutableStateOf(sessionState.addressCity) }
    var addressZip     by remember { mutableStateOf(sessionState.addressZip.replace("-", "")) }
    var addressCountry by remember { mutableStateOf(sessionState.addressCountry) }
    var avatarUrl      by remember { mutableStateOf(sessionState.avatarUrl) }

    val tenantId = sessionState.tenantId
    val isUploading by fileViewModel.isUploading.collectAsState()

    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent ?: return@rememberLauncherForActivityResult
            fileViewModel.uploadImage(
                context = context,
                tenantId = tenantId,
                uri = uri,
                onSuccess = { url ->
                    avatarUrl = url
                    // Save avatar URL using profile update
                    viewModel.updateProfile(
                        firstName = null, lastName = null, phone = null,
                        email = null, addressStreet = null, addressCity = null,
                        addressZip = null, addressCountry = null,
                        avatarUrl = url,
                        onSuccess = {
                            // State updated by AuthViewModel
                        },
                        onError = {}
                    )
                },
                onError = { msg ->
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    fun launchCropper() {
        cropLauncher.launch(
            CropImageContractOptions(
                uri = null,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeGallery = true,
                    imageSourceIncludeCamera = true,
                    cropShape = CropImageView.CropShape.OVAL,
                    fixAspectRatio = true,
                    aspectRatioX = 1,
                    aspectRatioY = 1,
                    outputCompressQuality = 85,
                    activityBackgroundColor = android.graphics.Color.parseColor("#1E1E1E")
                )
            )
        )
    }

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
    val zipError       = addressZip.isNotBlank() && !Regex("^[0-9]{5}$").matches(addressZip)
    val streetError    = addressStreet.isNotBlank() && addressStreet.length < 3
    val cityError      = addressCity.isNotBlank() && addressCity.length < 2
    val countryError   = addressCountry.isNotBlank() && addressCountry.length < 2

    val anyProfileField = firstName.isNotBlank() || lastName.isNotBlank() || email.isNotBlank()
            || phone.isNotBlank() || addressStreet.isNotBlank() || addressCity.isNotBlank()
            || addressZip.isNotBlank() || addressCountry.isNotBlank()
    val profileFormValid = !firstNameError && !lastNameError && !emailError && !phoneError
            && !zipError && !streetError && !cityError && !countryError && anyProfileField

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

            // Avatar
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { launchCropper() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Overlay camera icon
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { launchCropper() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Zmień zdjęcie",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            if (isUploading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Personal data
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

            // Address
            SectionHeader(icon = Icons.Default.Home, title = "Adres zamieszkania")

            OutlinedTextField(
                value = addressStreet,
                onValueChange = { addressStreet = it; profileError = null; profileSuccess = null },
                label = { Text("Ulica i numer") },
                leadingIcon = { Icon(Icons.Default.Place, null) },
                isError = streetError,
                supportingText = {
                    if (streetError) Text("Min. 3 znaki", color = MaterialTheme.colorScheme.error)
                },
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
                    isError = cityError,
                    supportingText = {
                        if (cityError) Text("Min. 2 znaki", color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = addressZip,
                    onValueChange = { input -> 
                        addressZip = input.filter { it.isDigit() }.take(5)
                        profileError = null
                        profileSuccess = null 
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
                    isError = zipError,
                    supportingText = {
                        if (zipError) Text("00-000", color = MaterialTheme.colorScheme.error)
                    },
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
                isError = countryError,
                supportingText = {
                    if (countryError) Text("Min. 2 znaki", color = MaterialTheme.colorScheme.error)
                },
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
                        addressZip     = addressZip.takeIf     { it.isNotBlank() }?.let { if (it.length == 5) "${it.take(2)}-${it.drop(2)}" else it },
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

            // Password change
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
