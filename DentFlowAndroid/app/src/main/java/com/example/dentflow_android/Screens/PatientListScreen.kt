package com.example.dentflow_android.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.ViewModel.PatientViewModel
import com.example.dentflow_android.data.remote.PatientResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(
    onBackClick: () -> Unit,
    viewModel: PatientViewModel = hiltViewModel()
) {
    val patients by viewModel.patients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedPatient by remember { mutableStateOf<PatientResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadPatients(1L)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Baza Pacjentów", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Powrót")
                    }
                }
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
                    items(patients) { patient ->
                        PatientItem(
                            patient = patient,
                            onEdit = {
                                selectedPatient = patient
                                showDialog = true
                            },
                            onDelete = {
                                viewModel.deletePatient(patient.id)
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
                onConfirm = { fName, lName, mail, phone ->
                    if (selectedPatient == null) {
                        viewModel.addPatient(fName, lName, mail, phone)
                    } else {
                        viewModel.updatePatient(selectedPatient!!.id, fName, lName, mail, phone)
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun PatientItem(
    patient: PatientResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${patient.firstName} ${patient.lastName}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                // POPRAWKA: Zmieniono z patient.phoneNumber na patient.phone
                Text(
                    text = "Tel: ${patient.phone ?: "Brak"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = patient.email ?: "Brak email",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Usuń",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PatientDialog(
    patient: PatientResponse?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var firstName by remember { mutableStateOf(patient?.firstName ?: "") }
    var lastName by remember { mutableStateOf(patient?.lastName ?: "") }
    var email by remember { mutableStateOf(patient?.email ?: "") }
    // POPRAWKA: Zmieniono inicjalizację z patient?.phoneNumber na patient?.phone
    var phone by remember { mutableStateOf(patient?.phone ?: "") }

    val isEmailValid = email.contains("@") && email.contains(".")
    val isPhoneValid = phone.length >= 9
    val areFieldsValid = firstName.isNotBlank() && lastName.isNotBlank() && isEmailValid && isPhoneValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (patient == null) "Dodaj pacjenta" else "Edytuj pacjenta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Imię") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Nazwisko") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(firstName, lastName, email, phone) },
                enabled = areFieldsValid
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
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
