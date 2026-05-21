package com.example.dentflow_android.Screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.ViewModel.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentScreen(
    viewModel: AppointmentViewModel = hiltViewModel(),
    patientViewModel: PatientViewModel = hiltViewModel(),
    staffViewModel: StaffViewModel = hiltViewModel(),
    catalogViewModel: CatalogViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel(),
    initialDoctorId: String = "",
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    
    // Load data
    LaunchedEffect(Unit) {
        patientViewModel.loadPatients()
        staffViewModel.loadStaff()
        catalogViewModel.loadServices()
        // rooms are loaded in tenantViewModel if tenantData is available
    }

    val patients by patientViewModel.patients.collectAsState()
    val staff by staffViewModel.staffMembers.collectAsState()
    val services by catalogViewModel.servicesState
    val tenantData by tenantViewModel.tenantState
    val rooms by tenantViewModel.rooms.collectAsState()
    val locations = tenantData?.locations ?: emptyList()

    var selectedPatientId by remember { mutableStateOf<Long?>(null) }
    var selectedServiceId by remember { mutableStateOf<Long?>(null) }
    var selectedDoctorId by remember { mutableStateOf(initialDoctorId.toLongOrNull()) }
    var selectedRoomId by remember { mutableStateOf<Long?>(null) }
    var selectedLocationId by remember { mutableStateOf<Long?>(null) }
    var notes by remember { mutableStateOf("") }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(12, 0)) }

    // Dropdown expanded states
    var patientExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }
    var doctorExpanded by remember { mutableStateOf(false) }
    var roomExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedTime = LocalTime.of(hourOfDay, minute)
        },
        selectedTime.hour,
        selectedTime.minute,
        true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Nowa Rezerwacja",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Patient Dropdown
        ExposedDropdownMenuBox(
            expanded = patientExpanded,
            onExpandedChange = { patientExpanded = it }
        ) {
            val patientName = patients.find { it.id == selectedPatientId }?.let { "${it.firstName} ${it.lastName}" } ?: "Wybierz pacjenta"
            OutlinedTextField(
                value = patientName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Pacjent") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientExpanded) },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = patientExpanded,
                onDismissRequest = { patientExpanded = false }
            ) {
                patients.forEach { patient ->
                    DropdownMenuItem(
                        text = { Text("${patient.firstName} ${patient.lastName} (${patient.phone})") },
                        onClick = {
                            selectedPatientId = patient.id
                            patientExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Service Dropdown
        ExposedDropdownMenuBox(
            expanded = serviceExpanded,
            onExpandedChange = { serviceExpanded = it }
        ) {
            val serviceName = services.find { it.id == selectedServiceId }?.name ?: "Wybierz usługę"
            OutlinedTextField(
                value = serviceName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Usługa (zabieg)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                leadingIcon = { Icon(Icons.Default.MedicalServices, null) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = serviceExpanded,
                onDismissRequest = { serviceExpanded = false }
            ) {
                services.filter { it.active }.forEach { service ->
                    DropdownMenuItem(
                        text = { Text(service.name) },
                        onClick = {
                            selectedServiceId = service.id
                            serviceExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Doctor Dropdown
        ExposedDropdownMenuBox(
            expanded = doctorExpanded,
            onExpandedChange = { doctorExpanded = it }
        ) {
            val doctorName = staff.find { it.id == selectedDoctorId }?.displayName ?: "Wybierz lekarza"
            OutlinedTextField(
                value = doctorName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Lekarz") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doctorExpanded) },
                leadingIcon = { Icon(Icons.Default.Badge, null) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = doctorExpanded,
                onDismissRequest = { doctorExpanded = false }
            ) {
                staff.forEach { member ->
                    DropdownMenuItem(
                        text = { Text("${member.displayName} (${member.profession})") },
                        onClick = {
                            selectedDoctorId = member.id
                            doctorExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Location Dropdown
            ExposedDropdownMenuBox(
                expanded = locationExpanded,
                onExpandedChange = { locationExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                val locName = locations.find { it.id == selectedLocationId }?.name ?: "Lokacja"
                OutlinedTextField(
                    value = locName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lokalizacja") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false }
                ) {
                    locations.forEach { loc ->
                        DropdownMenuItem(
                            text = { Text(loc.name) },
                            onClick = {
                                selectedLocationId = loc.id
                                locationExpanded = false
                            }
                        )
                    }
                }
            }

            // Room Dropdown
            ExposedDropdownMenuBox(
                expanded = roomExpanded,
                onExpandedChange = { roomExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                val roomName = rooms.find { it.id == selectedRoomId }?.name ?: "Pokój"
                OutlinedTextField(
                    value = roomName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gabinet") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = roomExpanded,
                    onDismissRequest = { roomExpanded = false }
                ) {
                    rooms.forEach { room ->
                        DropdownMenuItem(
                            text = { Text(room.name) },
                            onClick = {
                                selectedRoomId = room.id
                                roomExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Data i godzina wizyty", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            }
            OutlinedButton(
                onClick = { timePickerDialog.show() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notatki do wizyty") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val duration = services.find { it.id == selectedServiceId }?.durationMinutes ?: 30
                val startIso = "${selectedDate}T${selectedTime}:00Z"
                val endIso = "${selectedDate}T${selectedTime.plusMinutes(duration.toLong())}:00Z"

                viewModel.createAppointment(
                    locId = selectedLocationId ?: 0L,
                    room = selectedRoomId ?: 0L,
                    docId = selectedDoctorId ?: 0L,
                    patId = selectedPatientId ?: 0L,
                    servId = selectedServiceId ?: 0L,
                    start = startIso,
                    end = endIso,
                    note = notes,
                    onSuccess = onSuccess
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isLoading && selectedPatientId != null && selectedServiceId != null && selectedDoctorId != null && selectedLocationId != null && selectedRoomId != null
        ) {
            if (isLoading) CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White)
            else Text("ZAREZERWUJ WIZYTĘ")
        }
    }
}