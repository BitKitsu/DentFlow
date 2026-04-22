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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.ViewModel.StaffViewModel
import com.example.dentflow_android.data.remote.StaffMemberResponse
//import com.example.dentflow_android.ui.viewmodels.StaffViewModel

@Composable
fun AddStaffDialog(onDismiss: () -> Unit, onConfirm: (StaffMemberResponse) -> Unit) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowe Konto Pracownika") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Imię i Nazwisko") },
                    isError = showErrors && name.isBlank()
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Rola (np. Lekarz)") },
                    isError = showErrors && role.isBlank()
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail (Login)") },
                    isError = showErrors && !email.contains("@")
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Hasło tymczasowe") },
                    isError = showErrors && password.length < 6
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && email.contains("@") && password.length >= 6) {
                    // Tworzymy tymczasowy obiekt odpowiedzi (na potrzeby UI)
                    onConfirm(StaffMemberResponse(0L, 1L, 0L, name, role))
                } else { showErrors = true }
            }) { Text("DODAJ I REJESTRUJ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ANULUJ") }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    onBackClick: () -> Unit,
    viewModel: StaffViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    val staffList by viewModel.staffMembers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStaff(1L)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pracownicy i Grafiki", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White) }
        }
    ) { padding ->
        if (showDialog) {
            AddStaffDialog(
                onDismiss = { showDialog = false },
                onConfirm = { /* Tutaj w przyszłości dodasz wywołanie viewModel.addStaff(it) */
                    showDialog = false
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(staffList) { member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(45.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(member.displayName.take(1), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.displayName, fontWeight = FontWeight.Bold)
                            Text(member.profession, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { /* Tu wejdziemy w grafik */ }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
