package pl.edu.ur.dentflow.Screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.edu.ur.dentflow.data.ViewModel.ReportsViewModel
import pl.edu.ur.dentflow.data.ViewModel.VisitViewModel
import pl.edu.ur.dentflow.data.remote.PatientResponse
import pl.edu.ur.dentflow.data.remote.RoomResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBackClick: () -> Unit,
    visitViewModel: VisitViewModel,
    reportsViewModel: ReportsViewModel = hiltViewModel()
) {
    val patients by reportsViewModel.patients.collectAsState()
    val rooms by reportsViewModel.rooms.collectAsState()
    val isLoading by reportsViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        reportsViewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Raporty PDF", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ReportCard(
                icon = Icons.Default.PictureAsPdf,
                title = "Lista wizyt",
                description = "Raport listy wizyt w wybranym zakresie dat. Zawiera szczegóły pacjenta, lekarza, usługi i statusu."
            ) {
                ReportAppointmentList(visitViewModel = visitViewModel)
            }

            ReportCard(
                icon = Icons.Default.BarChart,
                title = "Obłożenie gabinetów",
                description = "Statystyki obłożenia gabinetów: wizyty dzienne, lekarze, top usługi, poziom absencji."
            ) {
                ReportRoomOccupancy(
                    visitViewModel = visitViewModel,
                    rooms = rooms
                )
            }

            ReportCard(
                icon = Icons.Default.Person,
                title = "Historia pacjenta",
                description = "Pełna historia wizyt wybranego pacjenta z danymi kontaktowymi i podsumowaniem."
            ) {
                ReportPatientHistory(
                    visitViewModel = visitViewModel,
                    patients = patients,
                    isLoading = isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReportCard(
    icon: ImageVector,
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            content()
        }
    }
}

@Composable
private fun ReportAppointmentList(visitViewModel: VisitViewModel) {
    val ctx = LocalContext.current
    var fromDate by remember { mutableStateOf(LocalDate.now().minusMonths(1)) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }

    val fromPicker = DatePickerDialog(ctx, { _, y, m, d ->
        fromDate = LocalDate.of(y, m + 1, d)
    }, fromDate.year, fromDate.monthValue - 1, fromDate.dayOfMonth)

    val toPicker = DatePickerDialog(ctx, { _, y, m, d ->
        toDate = LocalDate.of(y, m + 1, d)
    }, toDate.year, toDate.monthValue - 1, toDate.dayOfMonth)

    Text("Zakres dat:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { fromPicker.show() }, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE), style = MaterialTheme.typography.bodySmall)
        }
        OutlinedButton(onClick = { toPicker.show() }, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(toDate.format(DateTimeFormatter.ISO_LOCAL_DATE), style = MaterialTheme.typography.bodySmall)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = {
            visitViewModel.downloadReport(
                fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                toDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                ctx
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Download, null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Pobierz PDF")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportRoomOccupancy(
    visitViewModel: VisitViewModel,
    rooms: List<RoomResponse>
) {
    val ctx = LocalContext.current
    var fromDate by remember { mutableStateOf(LocalDate.now().minusMonths(1)) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedRoomId by remember { mutableStateOf<Long?>(null) }
    var roomExpanded by remember { mutableStateOf(false) }

    val fromPicker = DatePickerDialog(ctx, { _, y, m, d ->
        fromDate = LocalDate.of(y, m + 1, d)
    }, fromDate.year, fromDate.monthValue - 1, fromDate.dayOfMonth)

    val toPicker = DatePickerDialog(ctx, { _, y, m, d ->
        toDate = LocalDate.of(y, m + 1, d)
    }, toDate.year, toDate.monthValue - 1, toDate.dayOfMonth)

    Text("Zakres dat:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { fromPicker.show() }, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE), style = MaterialTheme.typography.bodySmall)
        }
        OutlinedButton(onClick = { toPicker.show() }, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(toDate.format(DateTimeFormatter.ISO_LOCAL_DATE), style = MaterialTheme.typography.bodySmall)
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    ExposedDropdownMenuBox(expanded = roomExpanded, onExpandedChange = { roomExpanded = it }) {
        OutlinedTextField(
            value = rooms.find { it.id == selectedRoomId }?.name ?: "Wszystkie gabinety",
            onValueChange = {},
            readOnly = true,
            label = { Text("Gabinet") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roomExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = roomExpanded, onDismissRequest = { roomExpanded = false }) {
            DropdownMenuItem(
                text = { Text("Wszystkie gabinety") },
                onClick = { selectedRoomId = null; roomExpanded = false }
            )
            rooms.forEach { room ->
                DropdownMenuItem(
                    text = { Text(room.name) },
                    onClick = { selectedRoomId = room.id; roomExpanded = false }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = {
            visitViewModel.downloadRoomOccupancyReport(
                fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                toDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                selectedRoomId,
                ctx
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Download, null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Pobierz PDF")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportPatientHistory(
    visitViewModel: VisitViewModel,
    patients: List<PatientResponse>,
    isLoading: Boolean
) {
    val ctx = LocalContext.current
    var selectedPatientId by remember { mutableStateOf<Long?>(null) }
    var patientExpanded by remember { mutableStateOf(false) }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        return
    }

    if (patients.isEmpty()) {
        Text("Brak pacjentów w gabinecie", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        return
    }

    ExposedDropdownMenuBox(expanded = patientExpanded, onExpandedChange = { patientExpanded = it }) {
        OutlinedTextField(
            value = patients.find { it.id == selectedPatientId }?.let { "${it.firstName} ${it.lastName}" }
                ?: "Wybierz pacjenta",
            onValueChange = {},
            readOnly = true,
            label = { Text("Pacjent") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(patientExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = patientExpanded, onDismissRequest = { patientExpanded = false }) {
            patients.forEach { patient ->
                DropdownMenuItem(
                    text = { Text("${patient.firstName} ${patient.lastName}") },
                    onClick = { selectedPatientId = patient.id; patientExpanded = false }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = {
            selectedPatientId?.let { pid ->
                visitViewModel.downloadPatientHistoryReport(pid, ctx)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        enabled = selectedPatientId != null
    ) {
        Icon(Icons.Default.Download, null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Pobierz PDF")
    }
}
