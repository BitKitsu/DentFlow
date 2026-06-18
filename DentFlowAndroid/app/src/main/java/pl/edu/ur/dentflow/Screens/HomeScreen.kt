package pl.edu.ur.dentflow.Screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.edu.ur.dentflow.R
import pl.edu.ur.dentflow.data.remote.*

// Model UI łączący dane dla jednego kafelka
data class ServiceDisplayModel(
    val service: ServiceCatalogItemDTO,
    val location: LocationResponse?,
    val specialists: List<StaffMemberResponse>,
    val clinicName: String = ""
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onStaffClick: (StaffMemberResponse) -> Unit,
    onBookClick: (Long, Long) -> Unit = { _, _ -> },
    staffList: List<StaffMemberResponse>,
    serviceList: List<ServiceCatalogItemDTO>,
    tenantData: TenantResponse?,
    allTenants: List<TenantResponse> = emptyList(),
    allCatalog: List<ServiceCatalogItemDTO> = emptyList(),
    allStaff: List<StaffMemberResponse> = emptyList(),
    isLoading: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }

    val displayItems = remember(searchQuery, allCatalog, allStaff, allTenants) {
        val q = searchQuery.lowercase().trim()

        val tenantsById = allTenants.associateBy { it.id }

        allCatalog
            .filter { it.active }
            .map { service ->
                val clinic = tenantsById[service.tenantId]
                val location = clinic?.locations?.firstOrNull()
                val specialists = allStaff.filter { it.tenantId == service.tenantId && !it.profession.lowercase().let { p -> p.contains("asystent") || p.contains("assistant") } }
                ServiceDisplayModel(service, location, specialists, clinic?.name ?: "")
            }
            .filter { item ->
                q.isEmpty() ||
                        item.service.name.lowercase().contains(q) ||
                        item.clinicName.lowercase().contains(q) ||
                        item.location?.addressCity?.lowercase()?.contains(q) == true ||
                        item.specialists.any { staff -> "${staff.firstName} ${staff.lastName}".lowercase().contains(q) }
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
            placeholder = { Text(stringResource(R.string.home_search), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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
                        ServiceTile(item, onStaffClick, onBookClick)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceTile(item: ServiceDisplayModel, onStaffClick: (StaffMemberResponse) -> Unit, onBookClick: (Long, Long) -> Unit = { _, _ -> }) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 8.dp else 2.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 0. Nazwa kliniki (jeśli więcej niż jedna)
            if (item.clinicName.isNotEmpty()) {
                Text(
                    text = item.clinicName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

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
                text = stringResource(R.string.home_available_specialists),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            if (item.specialists.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_no_doctors),
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
                            label = { Text("${staff.firstName} ${staff.lastName}", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(18.dp))
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.home_duration), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("${item.service.durationMinutes} min", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = { onBookClick(item.service.tenantId, item.service.id) },
                        enabled = item.specialists.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.home_book))
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
            Text(stringResource(R.string.home_no_services), color = Color.Gray, fontWeight = FontWeight.Medium)

            if (isError) {
                Text(
                    stringResource(R.string.home_auth_error),
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
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.home_welcome),
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
                contentDescription = stringResource(R.string.home_change_theme)
            )
        }
    }
}
