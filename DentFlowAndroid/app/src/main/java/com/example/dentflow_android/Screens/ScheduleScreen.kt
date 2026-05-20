package com.example.dentflow_android.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.ViewModel.ScheduleViewModel
import com.example.dentflow_android.data.ViewModel.TenantViewModel
import com.example.dentflow_android.data.ViewModel.VisitViewModel
import com.example.dentflow_android.data.remote.ScheduleSlotDTO
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel(),
    visitViewModel: VisitViewModel = hiltViewModel()
) {
    val slots by viewModel.slots.collectAsState()
    val visits by visitViewModel.visits.collectAsState()
    val isLoadingVisits by visitViewModel.isLoading.collectAsState()

    val tenantData by tenantViewModel.tenantState
    val rooms by tenantViewModel.rooms.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedSlot by remember { mutableStateOf<ScheduleSlotDTO?>(null) }
    var isAdding by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val locationMap = tenantData?.locations?.associate { it.id to it.name } ?: emptyMap()
    val roomMap = rooms.associate { it.id to it.name }

    val filteredVisits = remember(visits, selectedDate) {
        visits.filter { it.visit.startAt.take(10) == selectedDate.toString() }
            .sortedBy { it.visit.startAt }
    }

    LaunchedEffect(tenantData?.id) {
        viewModel.loadSchedule()
        visitViewModel.refreshVisits()
        tenantData?.id?.let { id ->
            tenantViewModel.loadRooms(id)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAdding = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, "Dodaj") }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "GRAFIK PRACY",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                                Icon(Icons.Default.ArrowBack, "Poprzedni", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("pl")).uppercase()} ${currentMonth.year}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                                Icon(Icons.Default.ArrowForward, "Następny", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    val dayHeaders = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd")
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        dayHeaders.forEach { day ->
                            Text(
                                day,
                                Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val firstDay = currentMonth.atDay(1)
                    val startOffset = firstDay.dayOfWeek.value - 1
                    val daysInMonth = currentMonth.lengthOfMonth()
                    val totalCells = startOffset + daysInMonth
                    val rows = (totalCells + 6) / 7

                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        repeat(rows) { row ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                repeat(7) { col ->
                                    val cellIndex = row * 7 + col
                                    val dayNumber = cellIndex - startOffset + 1

                                    if (dayNumber in 1..daysInMonth) {
                                        val date = currentMonth.atDay(dayNumber)
                                        val isSelected = date == selectedDate
                                        val isToday = date == LocalDate.now()

                                        Box(
                                            modifier = Modifier
                                                .weight(1f).height(40.dp).padding(2.dp)
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
                                                text = dayNumber.toString(),
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
                        text = selectedDate.format(
                            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("pl"))
                        ).uppercase(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isLoadingVisits) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredVisits.isEmpty()) {
                        item {
                            Text(
                                "Brak wizyt na ten dzień",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }

                    items(filteredVisits, key = { it.visit.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.width(70.dp)) {
                                    Text(
                                        text = try {
                                            item.visit.startAt.substringAfter("T").take(5)
                                        } catch (e: Exception) { "--:--" },
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = try {
                                            item.visit.endAt.substringAfter("T").take(5)
                                        } catch (e: Exception) { "--:--" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.patient?.let {
                                            "${it.firstName} ${it.lastName}"
                                        } ?: "Pacjent ID: ${item.visit.patientId}",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Zabieg ID: ${item.visit.serviceItemId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    if (!item.visit.notes.isNullOrBlank()) {
                                        Text(
                                            text = item.visit.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                val statusColor = when (item.visit.status.uppercase()) {
                                    "CONFIRMED" -> Color(0xFF4CAF50)
                                    "COMPLETED" -> Color.LightGray
                                    "CANCELLED" -> Color.Red
                                    else -> Color(0xFFFF9800)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAdding || selectedSlot != null) {
        SlotEditDialog(
            initialSlot = selectedSlot,
            locationMap = locationMap,
            roomMap = roomMap,
            currentTenantId = tenantData?.id ?: -1L,
            onDismiss = { isAdding = false; selectedSlot = null },
            onConfirm = { slotData ->
                if (selectedSlot != null) {
                    viewModel.updateSlot(selectedSlot!!.id, slotData)
                } else {
                    viewModel.addSlot(slotData)
                }
                isAdding = false; selectedSlot = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotEditDialog(
    initialSlot: ScheduleSlotDTO?,
    locationMap: Map<Long, String>,
    roomMap: Map<Long, String>,
    currentTenantId: Long,
    onDismiss: () -> Unit,
    onConfirm: (ScheduleSlotDTO) -> Unit
) {
    var date by remember { mutableStateOf(initialSlot?.startAt?.take(10) ?: LocalDate.now().toString()) }
    var startT by remember { mutableStateOf(initialSlot?.startAt?.substringAfter("T")?.take(5) ?: "10:00") }
    var endT by remember { mutableStateOf(initialSlot?.endAt?.substringAfter("T")?.take(5) ?: "10:30") }

    var locExpanded by remember { mutableStateOf(false) }
    var roomExpanded by remember { mutableStateOf(false) }

    var selectedLocId by remember(initialSlot, locationMap) {
        mutableStateOf(initialSlot?.locationId ?: locationMap.keys.firstOrNull() ?: 1L)
    }
    var selectedRoomId by remember(initialSlot, roomMap) {
        mutableStateOf(initialSlot?.roomId ?: roomMap.keys.firstOrNull() ?: 1L)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialSlot != null) "Edytuj Slot" else "Nowy Slot") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startT,
                        onValueChange = { startT = it },
                        label = { Text("Start") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endT,
                        onValueChange = { endT = it },
                        label = { Text("Koniec") },
                        modifier = Modifier.weight(1f)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = locExpanded,
                    onExpandedChange = { locExpanded = it }
                ) {
                    OutlinedTextField(
                        value = locationMap[selectedLocId] ?: "Wybierz lokalizację",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lokalizacja") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = locExpanded,
                        onDismissRequest = { locExpanded = false }
                    ) {
                        locationMap.forEach { (id, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = { selectedLocId = id; locExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = roomExpanded,
                    onExpandedChange = { roomExpanded = it }
                ) {
                    OutlinedTextField(
                        value = roomMap[selectedRoomId] ?: "Wybierz pokój",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pokój") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = roomExpanded,
                        onDismissRequest = { roomExpanded = false }
                    ) {
                        roomMap.forEach { (id, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = { selectedRoomId = id; roomExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    ScheduleSlotDTO(
                        id = initialSlot?.id ?: 0L,
                        tenantId = if (initialSlot != null && initialSlot.tenantId > 0) initialSlot.tenantId else currentTenantId,
                        staffId = initialSlot?.staffId ?: 1L,
                        locationId = selectedLocId,
                        roomId = selectedRoomId,
                        startAt = "${date}T${startT}:00Z",
                        endAt = "${date}T${endT}:00Z"
                    )
                )
            }) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}