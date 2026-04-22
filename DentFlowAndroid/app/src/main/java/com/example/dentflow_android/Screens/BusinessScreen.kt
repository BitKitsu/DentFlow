package com.example.dentflow_android.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.ViewModel.PatientViewModel
import com.example.dentflow_android.data.ViewModel.TenantViewModel
import com.example.dentflow_android.data.remote.LocationResponse

@Composable
fun BusinessScreen(
    tenantViewModel: TenantViewModel = hiltViewModel(),
    patientViewModel: PatientViewModel = hiltViewModel()
) {
    var showStaffManagement by remember { mutableStateOf(false) }
    var showPatientScreen by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // --- WCZYTYWANIE DANYCH ---
    LaunchedEffect(Unit) {
        tenantViewModel.loadTenantData(1L)
        patientViewModel.loadPatients(1L)
    }

    val tenantData by tenantViewModel.tenantState
    val patients by patientViewModel.patients.collectAsState()

    // --- LOGIKA WYŚWIETLANIA (Z OBSŁUGĄ BŁĘDU 403) ---
    val displayTenantName = when {
        tenantData == null -> "Brak dostępu lub ładowanie..."
        tenantData?.name == "string" -> "Klinika do konfiguracji"
        else -> tenantData?.name ?: "Moja Klinika"
    }

    val location = tenantData?.locations?.firstOrNull()
    val displayLocationName = when {
        tenantData == null -> "Zaloguj się ponownie (Błąd 403)"
        location == null -> "Nie znaleziono adresu"
        location.addressStreet == "string" -> "Adres nieustawiony"
        else -> "${location.addressStreet}, ${location.addressCity}"
    }

    val patientCount = patients.size.toString()

    when {
        showStaffManagement -> StaffManagementScreen(onBackClick = { showStaffManagement = false })
        showPatientScreen -> PatientListScreen(onBackClick = { showPatientScreen = false })
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                // --- NAGŁÓWEK ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayTenantName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = displayLocationName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (tenantData == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edytuj")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- STATYSTYKI ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), "Wizyty", "0", Icons.Default.Event, MaterialTheme.colorScheme.primary)
                    StatCard(Modifier.weight(1f), "Pacjenci", patientCount, Icons.Default.Group, MaterialTheme.colorScheme.secondary) {
                        showPatientScreen = true
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Zarządzanie", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { BusinessMenuItem("Pracownicy", Icons.Default.Badge) { showStaffManagement = true } }
                    item { BusinessMenuItem("Pacjenci", Icons.Default.People) { showPatientScreen = true } }
                    item { BusinessMenuItem("Cennik", Icons.Default.Payments) {} }

                }
            }
        }
    }

    if (showEditDialog) {
        EditTenantDialog(
            currentTenantName = if (tenantData?.name == "string") "" else tenantData?.name ?: "",
            currentLocation = location,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, loc, street, city, zip ->
                tenantViewModel.saveBusinessData(name, loc, street, city, zip)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditTenantDialog(
    currentTenantName: String,
    currentLocation: LocationResponse?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentTenantName) }
    var locName by remember { mutableStateOf(currentLocation?.name?.takeIf { it != "string" } ?: "") }
    var street by remember { mutableStateOf(currentLocation?.addressStreet?.takeIf { it != "string" } ?: "") }
    var city by remember { mutableStateOf(currentLocation?.addressCity?.takeIf { it != "string" } ?: "") }
    var zip by remember { mutableStateOf(currentLocation?.addressZip?.takeIf { it != "string" } ?: "") }

    val isZipValid = zip.length == 6 && zip.contains("-")
    val canSave = name.isNotBlank() && locName.isNotBlank() && street.isNotBlank() && city.isNotBlank() && isZipValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Konfiguracja Firmy") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa Firmy") })
                OutlinedTextField(value = locName, onValueChange = { locName = it }, label = { Text("Nazwa Lokalizacji") })
                OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Ulica") })
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Miasto") })
                OutlinedTextField(
                    value = zip,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 5) {
                            zip = if (digits.length > 2) "${digits.take(2)}-${digits.drop(2)}" else digits
                        }
                    },
                    label = { Text("Kod pocztowy") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = { Button(onClick = { onConfirm(name, locName, street, city, zip) }, enabled = canSave) { Text("Zapisz") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

// Funkcje StatCard i BusinessMenuItem (bez zmian)
@Composable
fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color, onClick: () -> Unit = {}) {
    Card(modifier = modifier.clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun BusinessMenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}
