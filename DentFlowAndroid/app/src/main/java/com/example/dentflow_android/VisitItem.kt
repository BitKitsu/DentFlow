package com.example.dentflow_android
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dentflow_android.data.Visit

@Composable
fun VisitItem(visit: Visit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Pacjent: ${visit.patientName}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Usługa: ${visit.serviceName}")
            Text(text = "Termin: ${visit.date}", color = Color.Gray)
            Text(
                text = "Status: ${visit.status}",
                color = if (visit.status == "CONFIRMED") Color.Blue else Color.Green
            )
        }
    }
}
