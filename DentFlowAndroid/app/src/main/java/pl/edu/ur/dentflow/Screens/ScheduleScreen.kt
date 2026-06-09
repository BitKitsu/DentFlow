package pl.edu.ur.dentflow.Screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import pl.edu.ur.dentflow.data.ViewModel.ScheduleViewModel
import pl.edu.ur.dentflow.data.ViewModel.StaffViewModel
import pl.edu.ur.dentflow.data.ViewModel.TenantViewModel
import pl.edu.ur.dentflow.data.remote.ScheduleBlockerDTO
import pl.edu.ur.dentflow.data.remote.StaffMemberResponse
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onBackClick: () -> Unit = {},
    isOwner: Boolean = true,
    viewModel: ScheduleViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel(),
    staffViewModel: StaffViewModel = hiltViewModel()
) {
    val blockers by viewModel.blockers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val tenantData by tenantViewModel.tenantState
    val staff by staffViewModel.staffMembers.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    var showBlockerDialog by remember { mutableStateOf(false) }
    var showDeleteBlockerConfirm by remember { mutableStateOf<ScheduleBlockerDTO?>(null) }

    LaunchedEffect(tenantData?.id) {
        viewModel.loadSchedule()
        staffViewModel.loadStaff()
    }

    val filteredBlockers = remember(blockers, selectedDate) {
        blockers.filter { it.startAt.take(10) == selectedDate.toString() }.sortedBy { it.startAt }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Przerwy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            if (isOwner) {
                FloatingActionButton(
                    onClick = { showBlockerDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) { Icon(Icons.Default.Add, "Dodaj") }
            }
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
                        listOf("Pn", "Wt", "Sr", "Cz", "Pt", "Sb", "Nd").forEach { d ->
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
            } else {
                BlockersList(
                    blockers = filteredBlockers,
                    staffList = staff,
                    isOwner = isOwner,
                    onDelete = { showDeleteBlockerConfirm = it }
                )
            }
        }
    }

    if (showBlockerDialog && isOwner) {
        BlockerAddDialog(
            staffList = staff,
            currentTenantId = tenantData?.id ?: -1L,
            selectedDate = selectedDate,
            onDismiss = { showBlockerDialog = false },
            onConfirm = { blocker ->
                viewModel.addBlocker(blocker)
                showBlockerDialog = false
            }
        )
    }

    showDeleteBlockerConfirm?.let { blocker ->
        AlertDialog(
            onDismissRequest = { showDeleteBlockerConfirm = null },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Usunac przerwe?") },
            text = { Text("Blokada zostanie usunieta.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteBlocker(blocker.id); showDeleteBlockerConfirm = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Usun") }
            },
            dismissButton = { TextButton(onClick = { showDeleteBlockerConfirm = null }) { Text("Anuluj") } }
        )
    }
}

@Composable
private fun BlockersList(
    blockers: List<ScheduleBlockerDTO>,
    staffList: List<StaffMemberResponse>,
    isOwner: Boolean = true,
    onDelete: (ScheduleBlockerDTO) -> Unit
) {
    if (blockers.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Block, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(8.dp))
                Text("Brak przerw / urlopow na ten dzien", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(blockers, key = { it.id }) { blocker ->
                val staffName = if (blocker.staffId <= 0) "Wszyscy" else staffList.find { it.id == blocker.staffId }?.let { "${it.firstName} ${it.lastName}" } ?: "Nieznany"
                val start = try { blocker.startAt.substringAfter("T").take(5) } catch (e: Exception) { "?" }
                val end = try { blocker.endAt.substringAfter("T").take(5) } catch (e: Exception) { "?" }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Block, null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(blocker.reason.ifBlank { "Przerwa" }, fontWeight = FontWeight.SemiBold)
                            Text("$staffName - $start - $end", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        if (isOwner) {
                            IconButton(onClick = { onDelete(blocker) }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockerAddDialog(
    staffList: List<StaffMemberResponse>,
    currentTenantId: Long,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (ScheduleBlockerDTO) -> Unit
) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(selectedDate.toString()) }
    var startT by remember { mutableStateOf("12:00") }
    var endT by remember { mutableStateOf("13:00") }
    var reason by remember { mutableStateOf("") }
    var staffExpanded by remember { mutableStateOf(false) }
    var selectedStaffId by remember { mutableStateOf<Long?>(null) }

    val startPicker = TimePickerDialog(context, { _, h, m -> startT = "%02d:%02d".format(h, m) }, 12, 0, true)
    val endPicker = TimePickerDialog(context, { _, h, m -> endT = "%02d:%02d".format(h, m) }, 13, 0, true)
    val cal = Calendar.getInstance()
    val datePicker = DatePickerDialog(context, { _, y, m, d ->
        date = LocalDate.of(y, m + 1, d).toString()
    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj przerwe / urlop", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { datePicker.show() }, Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.DateRange, null); Spacer(Modifier.width(8.dp)); Text(date)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { startPicker.show() }, Modifier.weight(1f)) { Text("Od: $startT") }
                    OutlinedButton(onClick = { endPicker.show() }, Modifier.weight(1f)) { Text("Do: $endT") }
                }
                OutlinedTextField(
                    value = reason, onValueChange = { reason = it },
                    label = { Text("Powod (np. urlop, przerwa)") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (staffList.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = staffExpanded, onExpandedChange = { staffExpanded = it }) {
                        OutlinedTextField(
                            value = staffList.find { it.id == selectedStaffId }?.let { "${it.firstName} ${it.lastName}" } ?: "Wszyscy (opcjonalnie)",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Lekarz") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(staffExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = staffExpanded, onDismissRequest = { staffExpanded = false }) {
                            DropdownMenuItem(text = { Text("Wszyscy") }, onClick = { selectedStaffId = null; staffExpanded = false })
                            staffList.forEach { s ->
                                DropdownMenuItem(text = { Text("${s.firstName} ${s.lastName}") }, onClick = { selectedStaffId = s.id; staffExpanded = false })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(ScheduleBlockerDTO(
                    id = 0L, tenantId = currentTenantId,
                    staffId = selectedStaffId ?: 0L, roomId = 0L,
                    startAt = "${date}T${startT}:00Z",
                    endAt = "${date}T${endT}:00Z",
                    reason = reason.ifBlank { "Przerwa" }
                ))
            }, shape = RoundedCornerShape(12.dp)) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}
