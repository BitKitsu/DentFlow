package com.example.dentflow_android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BusinessScreen() {
    // --- STANY NAWIGACJI ---
    var showStaffManagement by remember { mutableStateOf(false) }
    var showPatientScreen by remember { mutableStateOf(false) } // NOWY STAN

    // Logika wyboru ekranu
    when {
        showStaffManagement -> {
            StaffManagementScreen(onBackClick = { showStaffManagement = false })
        }
        showPatientScreen -> {
            PatientListScreen(onBackClick = { showPatientScreen = false })
        }
        else -> {
            // Główny widok panelu firmy
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                // --- NAGŁÓWEK ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Moja Klinika",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "DentFlow Clinic Rzeszów",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { /* Opcje firmy */ }) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- STATYSTYKI ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Wizyty",
                        value = "12",
                        icon = Icons.Default.Event,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { /* Opcjonalnie: kalendarz */ }
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Pacjenci", // KLIKALNY KAFELEK
                        value = "148",
                        icon = Icons.Default.Group,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = { showPatientScreen = true } // PRZEŁĄCZA NA LISTĘ
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- ZARZĄDZANIE ---
                Text(
                    text = "Zarządzanie",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        BusinessMenuItem(
                            title = "Pracownicy i grafiki",
                            icon = Icons.Default.Badge,
                            onClick = { showStaffManagement = true }
                        )
                    }
                    item {
                        BusinessMenuItem(
                            title = "Baza pacjentów",
                            icon = Icons.Default.People,
                            onClick = { showPatientScreen = true } // DODANO DO MENU
                        )
                    }
                    item { BusinessMenuItem("Usługi i cennik", Icons.Default.Payments, onClick = {}) }
                    item { BusinessMenuItem("Opinie pacjentów", Icons.Default.Star, onClick = {}) }
                    item { BusinessMenuItem("Raporty finansowe", Icons.Default.Analytics, onClick = {}) }
                }
            }
        }
    }
}

// --- POMOCNICZE (zaktualizowane o onClick) ---

@Composable
fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BusinessMenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}