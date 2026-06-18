package pl.edu.ur.dentflow.Screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.edu.ur.dentflow.R
import pl.edu.ur.dentflow.data.ViewModel.TenantViewModel
import pl.edu.ur.dentflow.data.remote.RoomResponse
import pl.edu.ur.dentflow.data.remote.StaffMemberResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomManagementScreen(
    onBackClick: () -> Unit,
    isOwner: Boolean = true,
    isReceptionist: Boolean = false,
    tenantViewModel: TenantViewModel = hiltViewModel(),
    staffViewModel: pl.edu.ur.dentflow.data.ViewModel.StaffViewModel = hiltViewModel()
) {
    val rooms by tenantViewModel.rooms.collectAsState()
    val staffMembers by staffViewModel.staffMembers.collectAsState()
    val tenantData by tenantViewModel.tenantState
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingRoom by remember { mutableStateOf<RoomResponse?>(null) }

    val locationId = tenantData?.locations?.firstOrNull()?.id ?: 1L

    LaunchedEffect(Unit) {
        tenantViewModel.loadRooms(tenantViewModel.currentTenantId)
        staffViewModel.loadStaff()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rooms_title), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            if (isOwner || isReceptionist) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
            }
        },
    ) { padding ->
        if (rooms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.rooms_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rooms) { room ->
                    RoomCard(
                        room = room,
                        staffMembers = staffMembers,
                        isOwner = isOwner,
                        isReceptionist = isReceptionist,
                        onEdit = { editingRoom = room },
                        onDelete = { tenantViewModel.deleteRoom(room.id) },
                        onToggleStaff = { staffId, isAssigned ->
                            if (isAssigned) {
                                tenantViewModel.removeStaffFromRoom(room.id, staffId)
                            } else {
                                tenantViewModel.assignStaffToRoom(room.id, staffId)
                            }
                        }
                    )
                }
            }
        }

        if (showCreateDialog) {
            CreateRoomDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name ->
                    tenantViewModel.createRoom(name, locationId)
                    showCreateDialog = false
                }
            )
        }

        editingRoom?.let { room ->
            EditRoomDialog(
                room = room,
                onDismiss = { editingRoom = null },
                onConfirm = { name, locationId ->
                    tenantViewModel.updateRoom(room.id, name, locationId)
                    editingRoom = null
                }
            )
        }
    }
}

@Composable
fun RoomCard(
    room: RoomResponse,
    staffMembers: List<StaffMemberResponse>,
    isOwner: Boolean = true,
    isReceptionist: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStaff: (Long, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val assignedStaff = staffMembers.filter { it.id in room.assignedStaffIds }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.MeetingRoom,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (assignedStaff.isNotEmpty()) {
                            val fmt = stringResource(R.string.doctor_format)
                            assignedStaff.joinToString(", ") { String.format(fmt, it.firstName, it.lastName) }
                        } else stringResource(R.string.rooms_no_doctors),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = stringResource(R.string.rooms_toggle_staff)
                    )
                }
                if (isOwner || isReceptionist) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, stringResource(R.string.rooms_edit))
                    }
                }
                if (isOwner) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, stringResource(R.string.rooms_delete), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.rooms_assigned_doctors),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                val dentists = staffMembers.filter {
                    val prof = it.profession.lowercase()
                    !prof.contains("asystent") && !prof.contains("assistant")
                }

                dentists.forEach { staff ->
                    val isAssigned = staff.id in room.assignedStaffIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isOwner) Modifier.clickable { onToggleStaff(staff.id, isAssigned) }
                                else Modifier
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAssigned,
                            onCheckedChange = if (isOwner) {{ onToggleStaff(staff.id, isAssigned) }} else null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.doctor_format, staff.firstName, staff.lastName),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val isNameValid = name.isBlank() || name.length >= 2

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rooms_add_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.rooms_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = name.isNotBlank() && !isNameValid,
                    supportingText = { if (name.isNotBlank() && !isNameValid) Text(stringResource(R.string.rooms_name_error)) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && isNameValid) onConfirm(name) },
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank() && isNameValid
            ) { Text(stringResource(R.string.rooms_add_button)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.staff_cancel)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoomDialog(
    room: RoomResponse,
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf(room.name) }
    val isNameValid = name.isBlank() || name.length >= 2

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rooms_edit_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.rooms_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = name.isNotBlank() && !isNameValid,
                    supportingText = { if (name.isNotBlank() && !isNameValid) Text(stringResource(R.string.rooms_name_error)) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && isNameValid) onConfirm(name, room.locationId) },
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank() && isNameValid
            ) { Text(stringResource(R.string.rooms_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.staff_cancel)) }
        }
    )
}