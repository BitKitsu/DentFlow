package com.example.dentflow_android.Screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.ViewModel.AppointmentViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun CreateAppointmentScreen(
    viewModel: AppointmentViewModel = hiltViewModel(),
    initialDoctorId: String = "",
    onSuccess: () -> Unit
) {
    var patientId by remember { mutableStateOf("") }
    var serviceId by remember { mutableStateOf("") }
    var doctorId by remember { mutableStateOf(initialDoctorId) }
    var roomId by remember { mutableStateOf("") }
    var locationId by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(12, 0)) }

    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Nowa Rezerwacja",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = patientId,
            onValueChange = { patientId = it },
            label = { Text("ID Pacjenta") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null) }
        )

        OutlinedTextField(
            value = serviceId,
            onValueChange = { serviceId = it },
            label = { Text("ID Usługi (np. Leczenie kanałowe)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.MedicalServices, null) }
        )

        OutlinedTextField(
            value = doctorId,
            onValueChange = { doctorId = it },
            label = { Text("ID Lekarza") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Badge, null) }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = roomId,
                onValueChange = { roomId = it },
                label = { Text("ID Pokoju") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = locationId,
                onValueChange = { locationId = it },
                label = { Text("ID Lokacji") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Data i godzina wizyty", style = MaterialTheme.typography.labelLarge)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { /* Tu opcjonalnie DatePicker */ },
                modifier = Modifier.weight(1f)
            ) {
                Text(selectedDate.toString())
            }
            Button(
                onClick = { /* Tu opcjonalnie TimePicker */ },
                modifier = Modifier.weight(1f)
            ) {
                Text(selectedTime.toString())
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notatki do wizyty") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val startIso = "${selectedDate}T${selectedTime}:00Z"
                val endIso = "${selectedDate}T${selectedTime.plusMinutes(30)}:00Z"

                viewModel.createAppointment(
                    locId = locationId.toLongOrNull() ?: 0L,
                    room = roomId.toLongOrNull() ?: 0L,
                    docId = doctorId.toLongOrNull() ?: 0L,
                    patId = patientId.toLongOrNull() ?: 0L,
                    servId = serviceId.toLongOrNull() ?: 0L,
                    start = startIso,
                    end = endIso,
                    note = notes,
                    onSuccess = onSuccess
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isLoading && patientId.isNotEmpty() && serviceId.isNotEmpty()
        ) {
            if (isLoading) CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White)
            else Text("ZAREZERWUJ WIZYTĘ")
        }
    }
}