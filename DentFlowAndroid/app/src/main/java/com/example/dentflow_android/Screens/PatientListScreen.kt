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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.ViewModel.PatientViewModel
import com.example.dentflow_android.data.ViewModel.VisitViewModel
import com.example.dentflow_android.data.remote.PatientResponse
import com.example.dentflow_android.data.remote.AppointmentResponse
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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

    LaunchedEffect(Unit) {
        viewModel.fetchPatients()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Baza Pacjentów", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Powrót")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                onConfirm = { fName, lName, mail, phone, dob, pesel, gender, street, city, zip ->
                    if (selectedPatient == null) {
                        viewModel.addPatient(fName, lName, mail, phone, dob, pesel, gender, street, city, zip, "Polska")
                    } else {
                        viewModel.updatePatient(selectedPatient!!.id, fName, lName, mail, phone, dob, pesel, gender, street, city, zip, "Polska")
                    }
                    showDialog = false
                }
            )
        }

        showHistoryFor?.let { patient ->
            PatientHistoryDialog(
                patient = patient,
                visitViewModel = visitViewModel,
                onDismiss = { showHistoryFor = null }
            )
        }
    }
}

@Composable
fun PatientItem(
    patient: PatientResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = patient.firstName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
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
                patient.dateOfBirth?.let {
                    Text(
                        text = "Ur.: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            IconButton(onClick = onShowHistory) {
                Icon(Icons.Default.History, contentDescription = "Historia wizyt", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDialog(
    patient: PatientResponse?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String?, String?, String?, String?, String?, String?) -> Unit
) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf(patient?.firstName ?: "") }
    var lastName by remember { mutableStateOf(patient?.lastName ?: "") }
    var email by remember { mutableStateOf(patient?.email ?: "") }
    var phone by remember { mutableStateOf(patient?.phone ?: "") }
    var dateOfBirth by remember { mutableStateOf(patient?.dateOfBirth ?: "") }
    
    // Nowe pola
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
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Imię") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Nazwisko") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail (opcjonalny)") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = !isEmailValid && email.isNotEmpty()
                )
                
                // --- SEKCJA MEDYCZNA ---
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Dane medyczne", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                
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
                                onClick = {
                                    gender = selectionOption
                                    expandedGender = false
                                }
                            )
                        }
                    }
                }
                // Data urodzenia — kliknięcie otwiera DatePickerDialog
                OutlinedTextField(
                    value = if (dateOfBirth.isNotBlank()) dateOfBirth else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data urodzenia (opcjonalna)") },
                    leadingIcon = { Icon(Icons.Default.Cake, null) },
                    trailingIcon = {
                        if (dateOfBirth.isNotBlank()) {
                            IconButton(onClick = { dateOfBirth = "" }) {
                                Icon(Icons.Default.Clear, null)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
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
                
                // --- SEKCJA ADRESOWA ---
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Adres (opcjonalny)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                
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
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(
                        firstName, lastName, email, phone, dateOfBirth.ifBlank { null },
                        pesel.ifBlank { null }, if (gender == "Nie podano") null else gender,
                        addressStreet.ifBlank { null }, addressCity.ifBlank { null }, addressZip.ifBlank { null }
                    ) 
                },
                enabled = areFieldsValid,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Zapisz")
            }
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