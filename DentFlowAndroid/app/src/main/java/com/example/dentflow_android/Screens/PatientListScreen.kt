package com.example.dentflow_android.Screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.dentflow_android.data.ViewModel.PatientViewModel
import com.example.dentflow_android.data.ViewModel.VisitViewModel
import com.example.dentflow_android.data.remote.AuthResponse
import com.example.dentflow_android.data.remote.PatientResponse
import com.example.dentflow_android.data.remote.AppointmentResponse
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(
    onBackClick: () -> Unit,
    viewModel: PatientViewModel = hiltViewModel(),
    visitViewModel: VisitViewModel = hiltViewModel()
) {
    val patients by viewModel.patients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedPatient by remember { mutableStateOf<PatientResponse?>(null) }
    var showHistoryFor by remember { mutableStateOf<PatientResponse?>(null) }
    var showDetailFor by remember { mutableStateOf<PatientResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchPatients()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Baza Pacjentów", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Powrót")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    selectedPatient = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj pacjenta")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (patients.isEmpty()) {
                EmptyPatientsState(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(patients, key = { it.id }) { patient ->
                        PatientItem(
                            patient = patient,
                            onClick = { showDetailFor = patient },
                            onEdit = {
                                selectedPatient = patient
                                showDialog = true
                            },
                            onDelete = { viewModel.deletePatient(patient.id) },
                            onShowHistory = {
                                showHistoryFor = patient
                                visitViewModel.fetchPatientHistory(patient.id)
                            }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            PatientDialog(
                patient = selectedPatient,
                onDismiss = { showDialog = false },
                onConfirm = { fName, lName, mail, phone, dob, pesel, gender, street, city, zip, exists, userId, avatarUrl ->
                    if (selectedPatient == null) {
                        viewModel.addPatient(fName, lName, mail, phone, dob, pesel, gender, street, city, zip, "Polska", userId, avatarUrl)
                    } else {
                        viewModel.updatePatient(selectedPatient!!.id, fName, lName, mail, phone, dob, pesel, gender, street, city, zip, "Polska")
                    }
                    showDialog = false
                },
                onCheckEmail = { email -> viewModel.checkUserByEmail(email) }
            )
        }

        showHistoryFor?.let { patient ->
            PatientHistoryDialog(
                patient = patient,
                visitViewModel = visitViewModel,
                onDismiss = { showHistoryFor = null }
            )
        }

        showDetailFor?.let { patient ->
            PatientDetailDialog(
                patient = patient,
                onDismiss = { showDetailFor = null }
            )
        }
    }
}

@Composable
fun PatientItem(
    patient: PatientResponse,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowHistory: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!patient.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = patient.avatarUrl,
                        contentDescription = "${patient.firstName} avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = patient.firstName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${patient.firstName} ${patient.lastName}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tel: ${patient.phone ?: "Brak"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edytuj") },
                        onClick = { showMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Historia wizyt") },
                        onClick = { showMenu = false; onShowHistory() },
                        leadingIcon = { Icon(Icons.Default.History, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Usuń", color = Color.Red) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDialog(
    patient: PatientResponse?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String?, String?, String?, String?, String?, String?, Boolean, Long?, String?) -> Unit,
    onCheckEmail: suspend (String) -> AuthResponse?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isEditing = patient != null

    var email by remember { mutableStateOf(patient?.email ?: "") }
    var emailChecked by remember { mutableStateOf(isEditing) }
    var userExists by remember { mutableStateOf(false) }
    var existingUserId by remember { mutableStateOf<Long?>(null) }
    var userAvatarUrl by remember { mutableStateOf<String?>(null) }
    var isCheckingEmail by remember { mutableStateOf(false) }

    var firstName by remember { mutableStateOf(patient?.firstName ?: "") }
    var lastName by remember { mutableStateOf(patient?.lastName ?: "") }
    var phone by remember { mutableStateOf(patient?.phone ?: "") }
    var dateOfBirth by remember { mutableStateOf(patient?.dateOfBirth ?: "") }
    var pesel by remember { mutableStateOf(patient?.pesel ?: "") }
    var gender by remember { mutableStateOf(patient?.gender ?: "Nie podano") }
    var addressStreet by remember { mutableStateOf(patient?.addressStreet ?: "") }
    var addressCity by remember { mutableStateOf(patient?.addressCity ?: "") }
    var addressZip by remember { mutableStateOf(patient?.addressZip ?: "") }

    val isEmailValid = email.isEmpty() || (email.contains("@") && email.contains("."))
    val isPhoneValid = phone.length >= 9
    val isPeselValid = pesel.isEmpty() || pesel.length == 11
    val areFieldsValid = firstName.isNotBlank() && lastName.isNotBlank() && isPhoneValid && isPeselValid

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            dateOfBirth = LocalDate.of(year, month + 1, dayOfMonth)
                .format(DateTimeFormatter.ISO_LOCAL_DATE)
        },
        calendar.get(Calendar.YEAR) - 30,
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (patient == null) "Dodaj pacjenta" else "Edytuj pacjenta") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                if (!isEditing) {
                    // Email check section (only for new patients)
                    Text("1. Sprawdź email pacjenta", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; emailChecked = false },
                            label = { Text("Email") },
                            modifier = Modifier.weight(1f),
                            isError = !isEmailValid && email.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !emailChecked
                        )
                        Button(
                            onClick = {
                                if (isEmailValid) {
                                    isCheckingEmail = true
                                    scope.launch {
                                        val userData = onCheckEmail(email)
                                        if (userData != null) {
                                            userExists = true
                                            existingUserId = userData.userId
                                            userAvatarUrl = userData.avatarUrl
                                            firstName = userData.firstName ?: ""
                                            lastName = userData.lastName ?: ""
                                            phone = userData.phone ?: ""
                                        } else {
                                            userExists = false
                                            existingUserId = null
                                            userAvatarUrl = null
                                        }
                                        emailChecked = true
                                        isCheckingEmail = false
                                    }
                                }
                            },
                            enabled = isEmailValid && !emailChecked && !isCheckingEmail,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            if (isCheckingEmail) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("Sprawdź")
                        }
                    }
                    if (emailChecked) {
                        if (userExists) {
                            Text("✓ Użytkownik istnieje - dane zostaną automatycznie uzupełnione", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text("⚠ Nowy pacjent", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (emailChecked || isEditing) {
                // Personal data section
                Text(if (isEditing) "Dane osobowe" else "2. Dane osobowe", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Imię") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !userExists || isEditing
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Nazwisko") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !userExists || isEditing
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !userExists || isEditing
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail (opcjonalny)") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = !isEmailValid && email.isNotEmpty(),
                    enabled = !userExists || isEditing
                )
                
                // Medical section
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text("3. Dane medyczne", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = pesel,
                    onValueChange = { if (it.length <= 11) pesel = it.filter { char -> char.isDigit() } },
                    label = { Text("Numer PESEL (11 cyfr)") },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = !isPeselValid && pesel.isNotEmpty()
                )
                if (!isPeselValid && pesel.isNotEmpty()) {
                    Text("PESEL musi zawierać dokładnie 11 cyfr", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                var expandedGender by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedGender,
                    onExpandedChange = { expandedGender = !expandedGender }
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Płeć") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGender,
                        onDismissRequest = { expandedGender = false }
                    ) {
                        listOf("Kobieta", "Mężczyzna", "Inna", "Nie podano").forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = { gender = selectionOption; expandedGender = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = if (dateOfBirth.isNotBlank()) dateOfBirth else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data urodzenia (opcjonalna)") },
                    leadingIcon = { Icon(Icons.Default.Cake, null) },
                    trailingIcon = {
                        if (dateOfBirth.isNotBlank()) {
                            IconButton(onClick = { dateOfBirth = "" }) { Icon(Icons.Default.Clear, null) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                if (dateOfBirth.isBlank()) {
                    TextButton(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Wybierz datę urodzenia")
                    }
                }
                
                // Address section
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text("4. Adres (opcjonalny)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = addressStreet,
                    onValueChange = { addressStreet = it },
                    label = { Text("Ulica i numer") },
                    leadingIcon = { Icon(Icons.Default.Home, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = addressZip,
                        onValueChange = { addressZip = it },
                        label = { Text("Kod pocztowy") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = addressCity,
                        onValueChange = { addressCity = it },
                        label = { Text("Miasto") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                } // end if (emailChecked || isEditing)
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(
                        firstName, lastName, email, phone, dateOfBirth.ifBlank { null },
                        pesel.ifBlank { null }, if (gender == "Nie podano") null else gender,
                        addressStreet.ifBlank { null }, addressCity.ifBlank { null }, addressZip.ifBlank { null },
                        userExists, existingUserId, userAvatarUrl
                    ) 
                },
                enabled = areFieldsValid && (emailChecked || isEditing),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Zapisz") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHistoryDialog(
    patient: PatientResponse,
    visitViewModel: VisitViewModel,
    onDismiss: () -> Unit
) {
    val visits by visitViewModel.visits.collectAsState()
    val isLoading by visitViewModel.isLoading.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Historia wizyt",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${patient.firstName} ${patient.lastName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (visits.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(8.dp))
                        Text("Brak historii wizyt", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visits) { visitItem ->
                        val visit = visitItem.visit
                        val timeDisplay = try { visit.startAt.substring(0, 16).replace("T", " ") } catch (e: Exception) { "—" }
                        val statusColor = when (visit.status.uppercase()) {
                            "CONFIRMED" -> Color(0xFF4CAF50)
                            "COMPLETED" -> MaterialTheme.colorScheme.outline
                            "CANCELLED" -> MaterialTheme.colorScheme.error
                            else -> Color(0xFFFF9800)
                        }
                        val statusLabel = when (visit.status.uppercase()) {
                            "CONFIRMED" -> "Potwierdzona"
                            "COMPLETED" -> "Zakończona"
                            "CANCELLED" -> "Anulowana"
                            "SCHEDULED" -> "Zaplanowana"
                            else -> visit.status
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = timeDisplay, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "Usługa ID: ${visit.serviceItemId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPatientsState(modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Default.PersonOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Baza pacjentów jest pusta", color = MaterialTheme.colorScheme.outline)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailDialog(
    patient: PatientResponse,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profil pacjenta", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                // Avatar
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!patient.avatarUrl.isNullOrBlank()) {
                        AsyncImage(model = patient.avatarUrl, contentDescription = "${patient.firstName} avatar", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Text(text = patient.firstName.take(1).uppercase(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "${patient.firstName} ${patient.lastName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                // Info rows
                if (!patient.phone.isNullOrBlank()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(patient.phone, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (!patient.email.isNullOrBlank()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(patient.email, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                patient.dateOfBirth?.let {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cake, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (!patient.pesel.isNullOrBlank()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PESEL: ${patient.pesel}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                val fullAddress = listOfNotNull(patient.addressStreet, patient.addressZip, patient.addressCity).joinToString(", ")
                if (fullAddress.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(fullAddress, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Zamknij") }
        }
    )
}