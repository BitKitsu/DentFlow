package pl.edu.ur.dentflow.Screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.edu.ur.dentflow.data.ViewModel.StaffViewModel
import pl.edu.ur.dentflow.data.ViewModel.VisitViewModel
import pl.edu.ur.dentflow.R
import androidx.compose.ui.res.stringResource
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicAppointmentsScreen(
    onBackClick: () -> Unit,
    visitViewModel: VisitViewModel,
    staffViewModel: StaffViewModel = hiltViewModel()
) {
    val visits by visitViewModel.visits.collectAsState()
    val staff by staffViewModel.staffMembers.collectAsState()
    val isLoading by visitViewModel.isLoading.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    var showCancelConfirm by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        visitViewModel.refreshVisits()
        staffViewModel.loadStaff()
    }

    val filteredVisits = remember(visits, selectedDate) {
        visits.filter { it.visit.startAt.take(10) == selectedDate.toString() }.sortedBy { it.visit.startAt }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.clinic_appointments_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("pl")).uppercase()} ${currentMonth.year}",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp
                            )
                            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        listOf(stringResource(R.string.day_mon_short), stringResource(R.string.day_tue_short), stringResource(R.string.day_wed_short), stringResource(R.string.day_thu_short), stringResource(R.string.day_fri_short), stringResource(R.string.day_sat_short), stringResource(R.string.day_sun_short)).forEach { d ->
                            Text(d, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(6.dp))

                    val firstDay = currentMonth.atDay(1)
                    val startOffset = firstDay.dayOfWeek.value - 1
                    val daysInMonth = currentMonth.lengthOfMonth()
                    val rows = (startOffset + daysInMonth + 6) / 7
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        repeat(rows) { row ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                repeat(7) { col ->
                                    val dayNum = row * 7 + col - startOffset + 1
                                    if (dayNum in 1..daysInMonth) {
                                        val date = currentMonth.atDay(dayNum)
                                        val isSelected = date == selectedDate
                                        val isToday = date == LocalDate.now()
                                        Box(
                                            modifier = Modifier.weight(1f).height(40.dp).padding(2.dp)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                    else if (isToday) MaterialTheme.colorScheme.primary.copy(0.15f)
                                                    else Color.Transparent,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .clickable { selectedDate = date },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                dayNum.toString(),
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    } else {
                                        Box(modifier = Modifier.weight(1f).height(40.dp))
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("pl"))).uppercase(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (filteredVisits.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.clinic_appointments_empty), color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredVisits) { v ->
                        val st = staff.find { it.id == v.visit.dentistStaffId }
                        val isScheduled = v.visit.status.uppercase() == "SCHEDULED"
                        val isConfirmed = v.visit.status.uppercase() == "CONFIRMED"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "${v.visit.startAt.substring(11, 16)} - ${v.visit.endAt.substring(11, 16)}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(stringResource(R.string.clinic_appointments_patient, v.patient?.firstName ?: "", v.patient?.lastName ?: ""))
                                    }
                                    val (statusColor, statusLabel) = when (v.visit.status.uppercase()) {
                                        "CONFIRMED" -> Pair(Color(0xFF4CAF50), stringResource(R.string.status_confirmed))
                                        "COMPLETED" -> Pair(MaterialTheme.colorScheme.outline, stringResource(R.string.status_completed))
                                        "CANCELLED" -> Pair(MaterialTheme.colorScheme.error, stringResource(R.string.status_cancelled))
                                        "SCHEDULED" -> Pair(Color(0xFFFF9800), stringResource(R.string.status_planned))
                                        else -> Pair(MaterialTheme.colorScheme.outline, v.visit.status)
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                                        Text(
                                            statusLabel,
                                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                if (st != null) Text(
                                    stringResource(R.string.clinic_appointments_doctor, st.firstName, st.lastName),
                                    style = MaterialTheme.typography.bodySmall
                                )

                                if (!visitViewModel.isReadOnly && (isScheduled || isConfirmed)) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isScheduled) {
                                            Button(
                                                onClick = { visitViewModel.confirmAppointment(v.visit.id) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                            ) {
                                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(stringResource(R.string.clinic_appointments_confirm), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (isConfirmed) {
                                            Button(
                                                onClick = { visitViewModel.completeAppointment(v.visit.id) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(stringResource(R.string.clinic_appointments_complete), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { showCancelConfirm = v.visit.id },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(stringResource(R.string.clinic_appointments_cancel), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showCancelConfirm?.let { appointmentId ->
        AlertDialog(
            onDismissRequest = { showCancelConfirm = null },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.clinic_appointments_cancel_title)) },
            text = { Text(stringResource(R.string.clinic_appointments_cancel_text)) },
            confirmButton = {
                Button(
                    onClick = { visitViewModel.cancelAppointment(appointmentId); showCancelConfirm = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(stringResource(R.string.clinic_appointments_cancel_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = null }) { Text(stringResource(R.string.clinic_appointments_leave)) }
            }
        )
    }
}
