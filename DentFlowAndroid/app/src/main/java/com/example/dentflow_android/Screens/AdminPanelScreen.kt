package com.example.dentflow_android.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.ViewModel.AdminViewModel

@Composable
fun AdminPanelScreen(
    onNavigateToStaff: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToSettings: () -> Unit,          // ← dodany parametr
    viewModel: AdminViewModel = hiltViewModel()
) {
    val visitCount by viewModel.visitCount.collectAsState()
    val patientCount by viewModel.patientCount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStats()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Panel Administratora",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Zarządzaj swoją kliniką",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Wizyty (łącznie)",
                value = visitCount,
                icon = Icons.Default.Event,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
            StatCard(
                label = "Wszyscy Pacjenci",
                value = patientCount,
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Zarządzanie",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                AdminActionCard(
                    title = "Pracownicy",
                    icon = Icons.Default.Badge,
                    subtitle = "Lista personelu",
                    onClick = onNavigateToStaff
                )
            }
            item {
                AdminActionCard(
                    title = "Pacjenci",
                    icon = Icons.Default.ContactPage,
                    subtitle = "Baza danych",
                    onClick = onNavigateToPatients
                )
            }
            item {
                AdminActionCard(
                    title = "Usługi",
                    icon = Icons.Default.ListAlt,
                    subtitle = "Cennik",
                    onClick = onNavigateToCatalog
                )
            }
            item {
                AdminActionCard(
                    title = "Ustawienia",
                    icon = Icons.Default.Settings,
                    subtitle = "Konfiguracja",
                    onClick = onNavigateToSettings   // ← poprawione
                )
            }
            item {
                AdminActionCard(
                    title = "Grafik",
                    icon = Icons.Default.DateRange,
                    subtitle = "Harmonogram pracy",
                    onClick = onNavigateToSchedule
                )
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminActionCard(
    title: String,
    icon: ImageVector,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}