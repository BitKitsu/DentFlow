package com.example.dentflow_android

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
import androidx.compose.ui.unit.sp

data class StaffMember(
    val id: Int,
    val name: String,
    val role: String,
    val email: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(onBackClick: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val staffList = remember { mutableStateListOf<StaffMember>() }

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
                onConfirm = { staffList.add(it); showDialog = false }
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
                        Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(member.color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Text(member.name.take(1), color = member.color, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, fontWeight = FontWeight.Bold)
                            Text(member.role, style = MaterialTheme.typography.bodySmall)
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

@Composable
fun AddStaffDialog(onDismiss: () -> Unit, onConfirm: (StaffMember) -> Unit) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowe Konto Pracownika") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BusinessInputField(value = name, onValueChange = { name = it }, label = "Imię i Nazwisko", icon = Icons.Default.Person, isError = showErrors && name.isBlank())
                BusinessInputField(value = role, onValueChange = { role = it }, label = "Rola (np. Lekarz)", icon = Icons.Default.Work, isError = showErrors && role.isBlank())
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                BusinessInputField(value = email, onValueChange = { email = it }, label = "E-mail (Login)", icon = Icons.Default.Email, isError = showErrors && !email.contains("@"))
                BusinessInputField(value = password, onValueChange = { password = it }, label = "Hasło tymczasowe", icon = Icons.Default.Lock, isError = showErrors && password.length < 6)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && email.contains("@") && password.length >= 6) {
                    onConfirm(StaffMember((0..100).random(), name, role, email, Color(0xFF4DB6AC)))
                } else { showErrors = true }
            }) { Text("DODAJ I REJESTRUJ", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANULUJ", color = Color.White) } }
    )
}