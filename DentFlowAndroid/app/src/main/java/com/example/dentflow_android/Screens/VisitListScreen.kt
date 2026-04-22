package com.example.dentflow_android.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dentflow_android.data.ViewModel.VisitViewModel
import com.example.dentflow_android.data.ViewModel.VisitWithPatient
import com.example.dentflow_android.data.ViewModel.*

@Composable
fun VisitListScreen(viewModel: VisitViewModel, modifier: Modifier = Modifier) {
    val visits by viewModel.visits.collectAsState()

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Text(
                    text = "DentFlow - Lista Wizyt",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        }
    ) { padding ->
        if (visits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Brak zaplanowanych wizyt")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(visits) { item ->
                    VisitItem(item)
                }
            }
        }
    }
}

@Composable
fun VisitItem(item: VisitWithPatient) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val patientName = if (item.patient != null) {
                "${item.patient.firstName} ${item.patient.lastName}"
            } else {
                "Pacjent nieznany (ID: ${item.visit.patientId})"
            }

            Text(
                text = patientName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Termin: ${item.visit.startTime}")

            item.visit.description?.let {
                Text(text = "Opis: $it")
            }

            Spacer(modifier = Modifier.height(8.dp))

            SuggestionChip(
                onClick = { },
                label = { Text(item.visit.status) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    labelColor = if (item.visit.status == "COMPLETED")
                        MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
