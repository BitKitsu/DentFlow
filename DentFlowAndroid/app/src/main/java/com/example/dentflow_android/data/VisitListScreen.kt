package com.example.dentflow_android.data

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dentflow_android.data.VisitViewModel

@Composable
fun VisitListScreen(viewModel: VisitViewModel, modifier: Modifier = Modifier) {
    val visits by viewModel.visits.collectAsState()

    Scaffold(
        topBar = { Text("DentFlow - Lista Wizyt", modifier = Modifier.padding(16.dp)) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(visits) { visit ->
                VisitItem(visit)
            }
        }
    }
}

@Composable
fun VisitItem(visit: Visit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = visit.patientName, style = MaterialTheme.typography.titleLarge)
            Text(text = "Lekarz: ${visit.doctorName}")
            Text(text = "Usługa: ${visit.serviceName}")
            Text(text = "Status: ${visit.status}")
        }
    }
}