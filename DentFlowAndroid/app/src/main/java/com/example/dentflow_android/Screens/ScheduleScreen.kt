package com.example.dentflow_android.Screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.dentflow_android.data.ViewModel.ScheduleViewModel
import com.example.dentflow_android.data.ViewModel.StaffViewModel
import com.example.dentflow_android.data.ViewModel.TenantViewModel
import com.example.dentflow_android.data.remote.ScheduleBlockerDTO
import com.example.dentflow_android.data.remote.ScheduleSlotDTO
import com.example.dentflow_android.data.remote.StaffMemberResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel(),
    staffViewModel: StaffViewModel = hiltViewModel()
) {
    val slots by viewModel.slots.collectAsState()
    val blockers by viewModel.blockers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val tenantData by tenantViewModel.tenantState
    val rooms by tenantViewModel.rooms.collectAsState()
    val staff by staffViewModel.staffMembers.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog state
    var showSlotDialog by remember { mutableStateOf(false) }
    var editingSlot by remember { mutableStateOf<ScheduleSlotDTO?>(null) }
    var showBlockerDialog by remember { mutableStateOf(false) }
    var showDeleteSlotConfirm by remember { mutableStateOf<ScheduleSlotDTO?>(null) }
    var showDeleteBlockerConfirm by remember { mutableStateOf<ScheduleBlockerDTO?>(null) }

    val locationMap = tenantData?.locations?.associate { it.id to it.name } ?: emptyMap()
    val roomMap = rooms.associate { it.id to it.name }

    LaunchedEffect(tenantData?.id) {
        viewModel.loadSchedule()
        staffViewModel.loadStaff()
        tenantData?.id?.let { tenantViewModel.loadRooms(it) }
    }

    val filteredSlots = remember(slots, selectedDate) {
        slots.filter { it.startAt.take(10) == selectedDate.toString() }.sortedBy { it.startAt }
    }
    val filteredBlockers = remember(blockers, selectedDate) {
        blockers.filter { it.startAt.take(10) == selectedDate.toString() }.sortedBy { it.startAt }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) { editingSlot = null; showSlotDialog = true }
                    else showBlockerDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, "Dodaj") }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header + calendar
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("GRAFIK PRACY", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                                Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("pl")).uppercase()} ${currentMonth.year}",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp
                            )
                            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                                Icon(Icons.Default.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Day headers
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        listOf("Pn","Wt","Śr","Cz","Pt","Sb","Nd").forEach { d ->
                            Text(d, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(6.dp))

                    // Calendar grid
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

                    // Tabs
                    TabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Sloty (${filteredSlots.size})") })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Przerwy (${filteredBlockers.size})") })
                    }
                }
            }

            // Content
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                when (selectedTab) {
                    0 -> SlotsList(
                        slots = filteredSlots,
                        staffList = staff,
                        roomMap = roomMap,
                        onEdit = { editingSlot = it; showSlotDialog = true },
                        onDelete = { showDeleteSlotConfirm = it }
                    )
                    1 -> BlockersList(
                        blockers = filteredBlockers,
                        staffList = staff,
                        onDelete = { showDeleteBlockerConfirm = it }
                    )
                }
            }
        }
    }

    // Slot dialog
    if (showSlotDialog) {
        SlotEditDialog(
            initialSlot = editingSlot,
            locationMap = locationMap,
            roomMap = roomMap,
            staffList = staff,
            currentTenantId = tenantData?.id ?: -1L,
            onDismiss = { showSlotDialog = false; editingSlot = null },
            onConfirm = { slotData ->
                if (editingSlot != null) viewModel.updateSlot(editingSlot!!.id, slotData)
                else viewModel.addSlot(slotData)
                showSlotDialog = false; editingSlot = null
            }
        )
    }

    // Blocker dialog
    if (showBlockerDialog) {
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

    // Delete slot confirm
    showDeleteSlotConfirm?.let { slot ->
        AlertDialog(
            onDismissRequest = { showDeleteSlotConfirm = null },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Usunąć slot?") },
            text = { Text("Slot zostanie trwale usunięty z grafiku.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteSlot(slot.id); showDeleteSlotConfirm = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Usuń") }
            },
            dismissButton = { TextButton(onClick = { showDeleteSlotConfirm = null }) { Text("Anuluj") } }
        )
    }

    // Delete blocker confirm
    showDeleteBlockerConfirm?.let { blocker ->
        AlertDialog(
            onDismissRequest = { showDeleteBlockerConfirm = null },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Usunąć przerwę?") },
            text = { Text("Blokada zostanie usunięta.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteBlocker(blocker.id); showDeleteBlockerConfirm = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Usuń") }
            },
            dismissButton = { TextButton(onClick = { showDeleteBlockerConfirm = null }) { Text("Anuluj") } }
        )
    }
}

@Composable
private fun SlotsList(
    slots: List<ScheduleSlotDTO>,
    staffList: List<StaffMemberResponse>,
    roomMap: Map<Long, String>,
    onEdit: (ScheduleSlotDTO) -> Unit,
    onDelete: (ScheduleSlotDTO) -> Unit
) {
    if (slots.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.EventBusy, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(8.dp))
                Text("Brak slotów na ten dzień", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(slots, key = { it.id }) { slot ->
                val staffName = staffList.find { it.id == slot.staffId }?.let { "${it.firstName} ${it.lastName}" } ?: "Lekarz ID: ${slot.staffId}"
                val roomName = roomMap[slot.roomId] ?: "Gabinet ID: ${slot.roomId}"
                val start = try { slot.startAt.substringAfter("T").take(5) } catch (e: Exception) { "?" }
                val end = try { slot.endAt.substringAfter("T").take(5) } catch (e: Exception) { "?" }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.width(70.dp)) {
                            Text(start, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(end, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(staffName, fontWeight = FontWeight.SemiBold)
                            Text(roomName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        IconButton(onClick = { onEdit(slot) }) {
                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onDelete(slot) }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockersList(
    blockers: List<ScheduleBlockerDTO>,
    staffList: List<StaffMemberResponse>,
    onDelete: (ScheduleBlockerDTO) -> Unit
) {
    if (blockers.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Block, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(8.dp))
                Text("Brak przerw / urlopów na ten dzień", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(blockers, key = { it.id }) { blocker ->
                val staffName = staffList.find { it.id == blocker.staffId }?.let { "${it.firstName} ${it.lastName}" } ?: "Wszyscy"
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
                            Text("$staffName • $start – $end", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        IconButton(onClick = { onDelete(blocker) }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotEditDialog(
    initialSlot: ScheduleSlotDTO?,
    locationMap: Map<Long, String>,
    roomMap: Map<Long, String>,
    staffList: List<StaffMemberResponse>,
    currentTenantId: Long,
    onDismiss: () -> Unit,
    onConfirm: (ScheduleSlotDTO) -> Unit
) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(initialSlot?.startAt?.take(10) ?: LocalDate.now().toString()) }
    var startT by remember { mutableStateOf(initialSlot?.startAt?.substringAfter("T")?.take(5) ?: "09:00") }
    var endT by remember { mutableStateOf(initialSlot?.endAt?.substringAfter("T")?.take(5) ?: "17:00") }

    var locExpanded by remember { mutableStateOf(false) }
    var roomExpanded by remember { mutableStateOf(false) }
    var staffExpanded by remember { mutableStateOf(false) }

    var selectedLocId by remember { mutableStateOf(initialSlot?.locationId ?: locationMap.keys.firstOrNull() ?: 1L) }
    var selectedRoomId by remember { mutableStateOf(initialSlot?.roomId ?: roomMap.keys.firstOrNull() ?: 1L) }
    var selectedStaffId by remember { mutableStateOf(initialSlot?.staffId ?: staffList.firstOrNull()?.id ?: 1L) }

    val cal = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(context, { _, y, m, d ->
        date = LocalDate.of(y, m + 1, d).toString()
    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

    val startPicker = TimePickerDialog(context, { _, h, m -> startT = "%02d:%02d".format(h, m) },
        startT.split(":")[0].toIntOrNull() ?: 9, startT.split(":")[1].toIntOrNull() ?: 0, true)
    val endPicker = TimePickerDialog(context, { _, h, m -> endT = "%02d:%02d".format(h, m) },
        endT.split(":")[0].toIntOrNull() ?: 17, endT.split(":")[1].toIntOrNull() ?: 0, true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialSlot != null) "Edytuj Slot" else "Nowy Slot", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Date
                OutlinedButton(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.DateRange, null); Spacer(Modifier.width(8.dp)); Text(date)
                }
                // Time
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { startPicker.show() }, Modifier.weight(1f)) { Text("Od: $startT") }
                    OutlinedButton(onClick = { endPicker.show() }, Modifier.weight(1f)) { Text("Do: $endT") }
                }
                // Staff dropdown
                if (staffList.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = staffExpanded, onExpandedChange = { staffExpanded = it }) {
                        OutlinedTextField(
                            value = staffList.find { it.id == selectedStaffId }?.let { "${it.firstName} ${it.lastName}" } ?: "Wybierz lekarza",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Lekarz") },
                            leadingIcon = { Icon(Icons.Default.Badge, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(staffExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = staffExpanded, onDismissRequest = { staffExpanded = false }) {
                            staffList.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.firstName} ${s.lastName} (${s.profession})") },
                                    onClick = { selectedStaffId = s.id; staffExpanded = false }
                                )
                            }
                        }
                    }
                }
                // Location dropdown
                if (locationMap.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = locExpanded, onExpandedChange = { locExpanded = it }) {
                        OutlinedTextField(
                            value = locationMap[selectedLocId] ?: "Wybierz lokalizację",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Lokalizacja") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(locExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = locExpanded, onDismissRequest = { locExpanded = false }) {
                            locationMap.forEach { (id, name) ->
                                DropdownMenuItem(text = { Text(name) }, onClick = { selectedLocId = id; locExpanded = false })
                            }
                        }
                    }
                }
                // Room dropdown
                if (roomMap.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = roomExpanded, onExpandedChange = { roomExpanded = it }) {
                        OutlinedTextField(
                            value = roomMap[selectedRoomId] ?: "Wybierz gabinet",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Gabinet") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roomExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = roomExpanded, onDismissRequest = { roomExpanded = false }) {
                            roomMap.forEach { (id, name) ->
                                DropdownMenuItem(text = { Text(name) }, onClick = { selectedRoomId = id; roomExpanded = false })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(ScheduleSlotDTO(
                    id = initialSlot?.id ?: 0L,
                    tenantId = if (initialSlot != null && initialSlot.tenantId > 0) initialSlot.tenantId else currentTenantId,
                    staffId = selectedStaffId,
                    locationId = selectedLocId,
                    roomId = selectedRoomId,
                    startAt = "${date}T${startT}:00Z",
                    endAt = "${date}T${endT}:00Z"
                ))
            }, shape = RoundedCornerShape(12.dp)) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
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
        title = { Text("Dodaj przerwę / urlop", fontWeight = FontWeight.Bold) },
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
                    label = { Text("Powód (np. urlop, przerwa)") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Optional: staff selection
                if (staffList.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = staffExpanded, onExpandedChange = { staffExpanded = it }) {
                        OutlinedTextField(
                            value = staffList.find { it.id == selectedStaffId }?.let { "${it.firstName} ${it.lastName}" } ?: "Wszyscy (opcjonalnie)",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Lekarz") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(staffExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
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