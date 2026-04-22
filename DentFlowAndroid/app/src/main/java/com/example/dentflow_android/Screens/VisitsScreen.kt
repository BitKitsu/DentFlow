package com.example.dentflow_android.Screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.dentflow_android.data.ViewModel.*
enum class VisitStatus {
    CONFIRMED, PENDING, COMPLETED
}

data class Visit(
    val id: Int,
    val patientName: String, // Dla pacjenta to będzie nazwa kliniki/lekarza
    val procedure: String,
    val time: String,
    val duration: String,
    val status: VisitStatus
)

@Composable
fun VisitsScreen(
    userRole: String = "OWNER" // "OWNER", "ADMIN", "DOCTOR", "PATIENT"
) {
    var selectedDate by remember { mutableStateOf(15) }
    var selectedDoctorFilter by remember { mutableStateOf("Wszyscy") }

    Scaffold(
        floatingActionButton = {
            if (userRole != "PATIENT") {
                FloatingActionButton(
                    onClick = { /* Dodaj wizytę */ },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // NAGŁÓWEK
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (userRole == "PATIENT") "Moje Wizyty" else "Terminarz Kliniki",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Październik 2023",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            // FILTR DLA ADMINA/OWNERA
            if (userRole == "OWNER" || userRole == "ADMIN") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    val doctors = listOf("Wszyscy", "dr Nowak", "dr Kowalski", "dr Wiśniewska")
                    items(doctors) { doc ->
                        FilterChip(
                            selected = selectedDoctorFilter == doc,
                            onClick = { selectedDoctorFilter = doc },
                            label = { Text(doc) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary ),
                            border = FilterChipDefaults.filterChipBorder( enabled = true,
                                selected = selectedDoctorFilter == doc,
                                selectedBorderColor = MaterialTheme.colorScheme.secondary,
                                borderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            // POZIOMY KALENDARZ (DayItem jest zdefiniowany niżej)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items((12..25).toList()) { day ->
                    DayItem(
                        day = day,
                        dayName = when(day % 7) {
                            1->"Pn"; 2->"Wt"; 3->"Śr"; 4->"Cz"; 5->"Pt"; 6->"So"; else->"Nd"
                        },
                        isSelected = selectedDate == day,
                        onSelect = { selectedDate = day }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // LISTA WIZYT
            val visits = listOf(
                Visit(1, "Anna Nowak", "Konsultacja", "09:00", "30 min", VisitStatus.COMPLETED),
                Visit(2, "Piotr Zieliński", "Leczenie kanałowe", "10:00", "60 min", VisitStatus.CONFIRMED),
                Visit(3, "Marek Wiśniewski", "Ekstrakcja", "12:30", "45 min", VisitStatus.PENDING)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visits) { visit ->
                    UniversalVisitCard(visit, userRole)
                }
            }
        }
    }
}

@Composable
fun DayItem(day: Int, dayName: String, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .width(55.dp)
            .height(80.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayName,
                fontSize = 12.sp,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = day.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun UniversalVisitCard(visit: Visit, userRole: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = visit.time,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(55.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = if (visit.status == VisitStatus.CONFIRMED)
                BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
            else null
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (userRole == "PATIENT") "Klinika: DentFlow" else visit.patientName,
                        fontWeight = FontWeight.Bold
                    )
                    Text(visit.procedure, style = MaterialTheme.typography.bodySmall)
                }

                val statusColor = when(visit.status) {
                    VisitStatus.CONFIRMED -> Color(0xFF4CAF50)
                    VisitStatus.PENDING -> Color(0xFFFF9800)
                    VisitStatus.COMPLETED -> Color.Gray
                }
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(statusColor))
            }
        }
    }
}
