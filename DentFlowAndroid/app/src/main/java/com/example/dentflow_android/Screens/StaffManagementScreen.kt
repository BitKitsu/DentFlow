package com.example.dentflow_android.Screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.ViewModel.StaffViewModel
import com.example.dentflow_android.data.remote.StaffMemberResponse

@Composable
fun AddStaffDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String) -> Unit
) {
    var fName by remember { mutableStateOf("") }
    var lName by remember { mutableStateOf("") }
    var prof by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val isEmailValid = email.contains("@") && email.contains(".")
    val isPassValid = pass.length >= 6

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj Pracownika", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fName,
                        onValueChange = { fName = it },
                        label = { Text("Imię") },
                        modifier = Modifier.weight(1f),
                        isError = showError && fName.isBlank(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = lName,
                        onValueChange = { lName = it },
                        label = { Text("Nazwisko") },
                        modifier = Modifier.weight(1f),
                        isError = showError && lName.isBlank(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                OutlinedTextField(
                    value = prof,
                    onValueChange = { prof = it },
                    label = { Text("Profesja") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && prof.isBlank(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Konto logowania)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && !isEmailValid,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Hasło tymczasowe") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && !isPassValid,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fName.isNotBlank() && lName.isNotBlank() && prof.isNotBlank() && isEmailValid && isPassValid) {
                        onConfirm(fName, lName, prof, email, pass, phone)
                    } else { showError = true }
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("DODAJ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ANULUJ") }
        }
    )
}

@Composable
fun EditStaffDialog(
    member: StaffMemberResponse,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    val names = member.displayName.split(" ")
    var fName by remember { mutableStateOf(names.firstOrNull() ?: "") }
    var lName by remember { mutableStateOf(if (names.size > 1) names.subList(1, names.size).joinToString(" ") else "") }
    var prof by remember { mutableStateOf(member.profession) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj dane", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = fName, onValueChange = { fName = it }, label = { Text("Imię") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = lName, onValueChange = { lName = it }, label = { Text("Nazwisko") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = prof, onValueChange = { prof = it }, label = { Text("Profesja") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(fName, lName, prof) }, shape = RoundedCornerShape(12.dp)) { Text("ZAPISZ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANULUJ") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    onBackClick: () -> Unit,
    viewModel: StaffViewModel = hiltViewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<StaffMemberResponse?>(null) }

    val staffList by viewModel.staffMembers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // --- POPRAWKA: Ładowanie bez 1L ---
    LaunchedEffect(Unit) {
        viewModel.loadStaff()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Zarządzanie Personelem", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            if (isLoading && staffList.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(staffList, key = { it.id }) { member ->
                        StaffItem(
                            member = member,
                            onEdit = { editingMember = member },
                            onDelete = { viewModel.deleteStaff(member.id) } // --- POPRAWKA ---
                        )
                    }
                }
            }

            if (showAddDialog) {
                AddStaffDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { fn, ln, pr, em, ps, ph ->
                        viewModel.addStaff(fn, ln, pr, em, ps, ph)
                        showAddDialog = false
                    }
                )
            }

            editingMember?.let { member ->
                EditStaffDialog(
                    member = member,
                    onDismiss = { editingMember = null },
                    onConfirm = { fn, ln, pr ->
                        // --- POPRAWKA: Bez tenantId ---
                        viewModel.updateStaff(member.id, fn, ln, pr, member.userId)
                        editingMember = null
                    }
                )
            }
        }
    }
}

@Composable
fun StaffItem(
    member: StaffMemberResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.displayName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.displayName,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = member.profession,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edytuj") },
                        onClick = { showMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Usuń z gabinetu", color = Color.Red) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}