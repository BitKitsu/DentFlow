package com.example.dentflow_android.Screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.ViewModel.AppointmentViewModel
import com.example.dentflow_android.data.ViewModel.CatalogViewModel
import com.example.dentflow_android.data.ViewModel.TenantViewModel
import com.example.dentflow_android.data.ViewModel.VisitViewModel
import com.example.dentflow_android.data.ViewModel.VisitWithPatient
import com.example.dentflow_android.data.remote.AppointmentResponse
import com.example.dentflow_android.data.remote.UpdateAppointmentRequest
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitsScreen(
    viewModel: VisitViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    catalogViewModel: CatalogViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel(),
    onCreateClick: () -> Unit = {}
) {
    val visits by viewModel.visits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val services by catalogViewModel.servicesState
    val rooms by tenantViewModel.rooms.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var isHistoryMode by remember { mutableStateOf(false) }
    var selectedVisit by remember { mutableStateOf<VisitWithPatient?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }
    var showCompleteConfirm by remember { mutableStateOf(false) }

    var showPdfDialog by remember { mutableStateOf(false) }
    var pdfFromDate by remember { mutableStateOf(LocalDate.now().minusMonths(1)) }
    var pdfToDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(selectedDate, isHistoryMode) {
        viewModel.refreshVisits()
    }
    LaunchedEffect(Unit) {
        catalogViewModel.loadServices()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ─── Header ────────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isHistoryMode) "Historia Wizyt" else "Kalendarz",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isHistoryMode) {
                        Text(
                            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pl")))
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row {
                    if (isHistoryMode) {
                        val ctx = LocalContext.current
                        IconButton(onClick = { showPdfDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Pobierz PDF",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { isHistoryMode = !isHistoryMode }) {
                        Icon(
                            imageVector = if (isHistoryMode) Icons.Default.CalendarMonth else Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // ─── Calendar ──────────────────────────────────────────────────────────
            if (!isHistoryMode) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null)
                    }
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM", Locale("pl"))).uppercase(),
                        style = MaterialTheme.typography.labelLarge
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }

                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.2f))

            // ─── Visits list ───────────────────────────────────────────────────────
            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val displayList = if (isHistoryMode) {
                    visits
                } else {
                    visits.filter { it.visit.startAt.startsWith(selectedDate.toString()) }
                }

                if (displayList.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(8.dp))
                            Text("Brak zarejestrowanych wizyt", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayList, key = { it.visit.id }) { item ->
                            UniversalVisitCard(
                                item = item,
                                showDate = isHistoryMode,
                                onClick = { selectedVisit = item }
                            )
                        }
                    }
                }
            }
        }

        // ─── FAB ───────────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = onCreateClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nowa wizyta")
        }
    }

    // ─── Appointment detail bottom sheet ───────────────────────────────────────
    selectedVisit?.let { visitWithPatient ->
        val visit = visitWithPatient.visit
        val canModify = visit.status.uppercase() in listOf("SCHEDULED", "CONFIRMED")
        val canComplete = visit.status.uppercase() == "CONFIRMED"

        ModalBottomSheet(onDismissRequest = { selectedVisit = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Szczegóły wizyty", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                // Patient
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = visitWithPatient.patient?.let { "${it.firstName} ${it.lastName}" } ?: "Pacjent ID: ${visit.patientId}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    val timeDisplay = try {
                        "${visit.startAt.substring(0, 10)} ${visit.startAt.substring(11, 16)} – ${visit.endAt.substring(11, 16)}"
                    } catch (e: Exception) { visit.startAt }
                    Text(text = timeDisplay, style = MaterialTheme.typography.bodyMedium)
                }

                // Service
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MedicalServices, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    val serviceName = services.find { it.id == visit.serviceItemId }?.name ?: "Usługa ID: ${visit.serviceItemId}"
                    Text(text = serviceName, style = MaterialTheme.typography.bodyMedium)
                }

                // Notes
                if (!visit.notes.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Notes, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(text = visit.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Status chip
                val (statusColor, statusLabel) = when (visit.status.uppercase()) {
                    "CONFIRMED" -> Pair(Color(0xFF4CAF50), "Potwierdzona")
                    "COMPLETED" -> Pair(MaterialTheme.colorScheme.outline, "Zakończona")
                    "CANCELLED" -> Pair(MaterialTheme.colorScheme.error, "Anulowana")
                    "SCHEDULED" -> Pair(Color(0xFFFF9800), "Zaplanowana")
                    else -> Pair(MaterialTheme.colorScheme.outline, visit.status)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                HorizontalDivider()

                // Action buttons
                if (canModify) {
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edytuj wizytę")
                    }
                }
                if (canComplete) {
                    OutlinedButton(
                        onClick = { showCompleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp), tint = Color(0xFF4CAF50))
                        Spacer(Modifier.width(8.dp))
                        Text("Zakończ wizytę", color = Color(0xFF4CAF50))
                    }
                }
                if (canModify) {
                    OutlinedButton(
                        onClick = { showCancelConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Anuluj wizytę")
                    }
                }
            }
        }
    }

    // ─── Edit dialog ───────────────────────────────────────────────────────────
    selectedVisit?.let { visitWithPatient ->
        if (showEditDialog) {
            EditAppointmentDialog(
                visit = visitWithPatient.visit,
                services = services,
                rooms = rooms,
                onDismiss = { showEditDialog = false },
                onConfirm = { startAt, endAt, servId, roomId, notes ->
                    appointmentViewModel.updateAppointment(
                        appointmentId = visitWithPatient.visit.id,
                        start = startAt,
                        end = endAt,
                        servId = servId,
                        roomId = roomId,
                        note = notes,
                        onSuccess = {
                            showEditDialog = false
                            selectedVisit = null
                            viewModel.refreshVisits()
                        }
                    )
                }
            )
        }
    }

    // ─── Cancel confirm ────────────────────────────────────────────────────────
    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            icon = { Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Anulować wizytę?") },
            text = { Text("Tej operacji nie można cofnąć. Pacjent otrzyma powiadomienie.") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedVisit?.let { v ->
                            appointmentViewModel.cancelAppointment(v.visit.id) {
                                showCancelConfirm = false
                                selectedVisit = null
                                viewModel.refreshVisits()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Anuluj wizytę") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) { Text("Wróć") }
            }
        )
    }

    // ─── Complete confirm ──────────────────────────────────────────────────────
    if (showCompleteConfirm) {
        AlertDialog(
            onDismissRequest = { showCompleteConfirm = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50)) },
            title = { Text("Zakończyć wizytę?") },
            text = { Text("Wizyta zostanie oznaczona jako zakończona.") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedVisit?.let { v ->
                            appointmentViewModel.completeAppointment(v.visit.id) {
                                showCompleteConfirm = false
                                selectedVisit = null
                                viewModel.refreshVisits()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Zakończ") }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteConfirm = false }) { Text("Anuluj") }
            }
        )
    }

    if (showPdfDialog) {
        val ctx = LocalContext.current
        val fromDatePicker = DatePickerDialog(
            ctx,
            { _, year, month, dayOfMonth ->
                pdfFromDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            pdfFromDate.year,
            pdfFromDate.monthValue - 1,
            pdfFromDate.dayOfMonth
        )
        val toDatePicker = DatePickerDialog(
            ctx,
            { _, year, month, dayOfMonth ->
                pdfToDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            pdfToDate.year,
            pdfToDate.monthValue - 1,
            pdfToDate.dayOfMonth
        )

        AlertDialog(
            onDismissRequest = { showPdfDialog = false },
            title = { Text("Zakres raportu PDF") },
            text = {
                Column {
                    OutlinedButton(onClick = { fromDatePicker.show() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Od: ${pdfFromDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { toDatePicker.show() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Do: ${pdfToDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPdfDialog = false
                    viewModel.downloadReport(
                        pdfFromDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        pdfToDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        ctx
                    )
                }) { Text("Pobierz") }
            },
            dismissButton = {
                TextButton(onClick = { showPdfDialog = false }) { Text("Anuluj") }
            }
        )
    }
}

// ─── Edit Appointment Dialog ────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppointmentDialog(
    visit: AppointmentResponse,
    services: List<com.example.dentflow_android.data.remote.ServiceCatalogItemDTO>,
    rooms: List<com.example.dentflow_android.data.remote.RoomResponse>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, Long, String) -> Unit
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(
        try { LocalDate.parse(visit.startAt.take(10)) } catch (e: Exception) { LocalDate.now() }
    ) }
    var selectedStartTime by remember { mutableStateOf(
        try { LocalTime.parse(visit.startAt.substring(11, 16)) } catch (e: Exception) { LocalTime.of(12, 0) }
    ) }
    var selectedEndTime by remember { mutableStateOf(
        try { LocalTime.parse(visit.endAt.substring(11, 16)) } catch (e: Exception) { LocalTime.of(13, 0) }
    ) }
    var selectedServiceId by remember { mutableStateOf(visit.serviceItemId) }
    var selectedRoomId by remember { mutableStateOf(visit.roomId) }
    var notes by remember { mutableStateOf(visit.notes ?: "") }
    var serviceExpanded by remember { mutableStateOf(false) }
    var roomExpanded by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day -> selectedDate = LocalDate.of(year, month + 1, day) },
        selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth
    )
    val startPickerDialog = TimePickerDialog(
        context,
        { _, h, m -> selectedStartTime = LocalTime.of(h, m) },
        selectedStartTime.hour, selectedStartTime.minute, true
    )
    val endPickerDialog = TimePickerDialog(
        context,
        { _, h, m -> selectedEndTime = LocalTime.of(h, m) },
        selectedEndTime.hour, selectedEndTime.minute, true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj wizytę", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Date
                OutlinedButton(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.DateRange, null)
                    Spacer(Modifier.width(8.dp))
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                }
                // Time
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { startPickerDialog.show() }, modifier = Modifier.weight(1f)) {
                        Text("Od: ${selectedStartTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                    }
                    OutlinedButton(onClick = { endPickerDialog.show() }, modifier = Modifier.weight(1f)) {
                        Text("Do: ${selectedEndTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                    }
                }
                // Service
                if (services.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = serviceExpanded, onExpandedChange = { serviceExpanded = it }) {
                        OutlinedTextField(
                            value = services.find { it.id == selectedServiceId }?.name ?: "Wybierz usługę",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Usługa") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = serviceExpanded, onDismissRequest = { serviceExpanded = false }) {
                            services.filter { it.active }.forEach { s ->
                                DropdownMenuItem(text = { Text(s.name) }, onClick = { selectedServiceId = s.id; serviceExpanded = false })
                            }
                        }
                    }
                }
                // Room
                if (rooms.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = roomExpanded, onExpandedChange = { roomExpanded = it }) {
                        OutlinedTextField(
                            value = rooms.find { it.id == selectedRoomId }?.name ?: "Wybierz gabinet",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Gabinet") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = roomExpanded, onDismissRequest = { roomExpanded = false }) {
                            rooms.forEach { r ->
                                DropdownMenuItem(text = { Text(r.name) }, onClick = { selectedRoomId = r.id; roomExpanded = false })
                            }
                        }
                    }
                }
                // Notes
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notatki") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val startIso = "${selectedDate}T${selectedStartTime}:00Z"
                    val endIso = "${selectedDate}T${selectedEndTime}:00Z"
                    onConfirm(startIso, endIso, selectedServiceId, selectedRoomId, notes)
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

// ─── Visit card ────────────────────────────────────────────────────────────────
@Composable
fun UniversalVisitCard(item: VisitWithPatient, showDate: Boolean = false, onClick: () -> Unit = {}) {
    val appointment = item.visit
    val patient = item.patient

    val timeDisplay = try { appointment.startAt.substring(11, 16) } catch (e: Exception) { "--:--" }
    val dateDisplay = try { appointment.startAt.substring(0, 10) } catch (e: Exception) { "" }

    val statusColor = when (appointment.status.uppercase()) {
        "CONFIRMED" -> Color(0xFF4CAF50)
        "COMPLETED" -> Color.LightGray
        "CANCELLED" -> Color.Red
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.width(70.dp)) {
                Text(text = timeDisplay, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (showDate) {
                    Text(text = dateDisplay, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patient?.let { "${it.firstName} ${it.lastName}" } ?: "Pacjent ID: ${appointment.patientId}",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Usługa ID: ${appointment.serviceItemId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(statusColor))
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

// ─── Calendar Grid ─────────────────────────────────────────────────────────────
@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value
    val days = (1..daysInMonth).toList()
    val weekDays = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(text = day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(220.dp),
            userScrollEnabled = false
        ) {
            items(firstDayOfMonth - 1) { Spacer(modifier = Modifier.fillMaxSize()) }
            items(days) { day ->
                val date = currentMonth.atDay(day)
                val isSelected = date == selectedDate
                val isToday = date == LocalDate.now()
                Box(
                    modifier = Modifier.aspectRatio(1f).padding(4.dp).clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = day.toString(), color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}