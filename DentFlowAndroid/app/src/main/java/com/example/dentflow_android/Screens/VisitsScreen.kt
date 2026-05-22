package com.example.dentflow_android.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CalendarMonth
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
import com.example.dentflow_android.data.ViewModel.VisitViewModel
import com.example.dentflow_android.data.ViewModel.VisitWithPatient
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun VisitsScreen(
    viewModel: VisitViewModel = hiltViewModel()
) {
    val visits by viewModel.visits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var isHistoryMode by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDate, isHistoryMode) {
        viewModel.refreshVisits()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isHistoryMode) "Historia Wizyt" else "Kalendarz",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (!isHistoryMode) {
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pl")))
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = { isHistoryMode = !isHistoryMode }) {
                Icon(
                    imageVector = if (isHistoryMode) Icons.Default.CalendarMonth else Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (!isHistoryMode) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                }
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM", Locale("pl"))).uppercase(),
                    style = MaterialTheme.typography.labelLarge
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.2f))

        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (visits.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Brak zarejestrowanych wizyt", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val displayList = if (isHistoryMode) {
                    visits
                } else {
                    visits.filter { it.visit.startAt.startsWith(selectedDate.toString()) }
                }

                items(displayList) { item ->
                    UniversalVisitCard(item, showDate = isHistoryMode)
                }
            }
        }
    }
}

@Composable
fun UniversalVisitCard(item: VisitWithPatient, showDate: Boolean = false) {
    val appointment = item.visit
    val patient = item.patient

    val timeDisplay = try { appointment.startAt.substring(11, 16) } catch (e: Exception) { "--:--" }
    val dateDisplay = try { appointment.startAt.substring(0, 10) } catch (e: Exception) { "" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.width(70.dp)) {
                Text(text = timeDisplay, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (showDate) {
                    Text(text = dateDisplay, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patient?.let { "${it.firstName} ${it.lastName}" } ?: "Pacjent ID: ${appointment.patientId}",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Zabieg ID: ${appointment.serviceItemId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            val statusColor = when (appointment.status.uppercase()) {
                "CONFIRMED" -> Color(0xFF4CAF50)
                "COMPLETED" -> Color.LightGray
                "CANCELLED" -> Color.Red
                else -> Color(0xFFFF9800)
            }
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(statusColor))
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value
    val days = (1..daysInMonth).toList()
    val weekDays = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(text = day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(220.dp),
            userScrollEnabled = false
        ) {
            items(firstDayOfMonth - 1) { Spacer(modifier = Modifier.fillMaxSize()) }
            items(days) { day ->
                val date = currentMonth.atDay(day)
                val isSelected = date == selectedDate
                val isToday = date == LocalDate.now()
                Box(
                    modifier = Modifier.aspectRatio(1f).padding(4.dp).clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = day.toString(), color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}