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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dentflow_android.data.remote.*

// Model UI łączący dane dla jednego kafelka
data class ServiceDisplayModel(
    val service: ServiceCatalogItemDTO,
    val location: LocationResponse?,
    val specialists: List<StaffMemberResponse>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onStaffClick: (StaffMemberResponse) -> Unit,
    staffList: List<StaffMemberResponse>,
    serviceList: List<ServiceCatalogItemDTO>,
    tenantData: TenantResponse?,
    isLoading: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }

    // Mapowanie danych: Zabieg -> Lokalizacja -> Specjaliści
    val displayItems = remember(searchQuery, staffList, serviceList, tenantData) {
        val q = searchQuery.lowercase().trim()

        serviceList
            .filter { it.active }
            .map { service ->
                val location = tenantData?.locations?.find { it.tenantId == service.tenantId }
                val specialists = staffList.filter { it.tenantId == service.tenantId }
                ServiceDisplayModel(service, location, specialists)
            }
            .filter { item ->
                q.isEmpty() ||
                        item.service.name.lowercase().contains(q) ||
                        item.location?.addressCity?.lowercase()?.contains(q) == true ||
                        item.specialists.any { staff -> staff.displayName.lowercase().contains(q) }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // --- NAGŁÓWEK ---
        HeaderSection(isDarkTheme, onThemeChange)

        Spacer(modifier = Modifier.height(16.dp))

        // --- WYSZUKIWARKA ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Zabieg, miasto lub nazwisko...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- OBSŁUGA STANÓW ---
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            displayItems.isEmpty() -> {
                EmptyStateSection(isError = serviceList.isEmpty())
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(displayItems, key = { it.service.id }) { item ->
                        ServiceTile(item, onStaffClick)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceTile(item: ServiceDisplayModel, onStaffClick: (StaffMemberResponse) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 1. Nazwa zabiegu i Cena
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Text(
                    text = item.service.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${item.service.priceCents / 100} zł",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 2. Lokalizacja
            item.location?.let { loc ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = Color(0xFFE91E63))
                    Text(
                        text = " ${loc.name} • ${loc.addressCity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // 3. Specjaliści
            Text(
                text = "Dostępni specjaliści:",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            if (item.specialists.isEmpty()) {
                Text(
                    text = "Brak przypisanych lekarzy",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.specialists.forEach { staff ->
                        AssistChip(
                            onClick = { onStaffClick(staff) },
                            label = { Text(staff.displayName, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(18.dp))
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateSection(isError: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Brak dostępnych usług", color = Color.Gray, fontWeight = FontWeight.Medium)

            if (isError) {
                Text(
                    "Błąd autoryzacji lub połączenia (403)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun HeaderSection(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "DentFlow",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Witaj w klinice",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
        IconButton(
            onClick = { onThemeChange(!isDarkTheme) },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.NightsStay,
                contentDescription = "Zmień motyw"
            )
        }
    }
}