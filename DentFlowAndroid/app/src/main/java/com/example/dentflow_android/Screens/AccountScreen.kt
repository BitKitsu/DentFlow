package com.example.dentflow_android.Screens

import android.net.Uri
import android.content.SharedPreferences
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.dentflow_android.data.ViewModel.TenantViewModel

@Composable
fun AccountScreen(
    tenantViewModel: TenantViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onEditBusinessClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Pobieramy dane sesji bezpośrednio z preferencji (tymczasowo, docelowo z UserViewModel)
    val prefs = remember { context.getSharedPreferences("dentflow_prefs", android.content.Context.MODE_PRIVATE) }
    val userEmail = remember { prefs.getString("user_email", "uzytkownik@dentflow.pl") ?: "" }
    val userRole = remember { prefs.getString("user_role", "STAFF") ?: "STAFF" }
    val isOwner = userRole == "OWNER" || userRole == "ADMIN"

    // --- WCZYTYWANIE DANYCH ---
    LaunchedEffect(Unit) {
        // Używamy nowej metody, która sama bierze ID z SharedPreferences
        tenantViewModel.loadAllTenantData()
    }

    val tenantData by tenantViewModel.tenantState

    // --- LOGIKA WYBORU ZDJĘĆ ---
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var companyLogoUri by remember { mutableStateOf<Uri?>(null) }

    val profilePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        profileImageUri = uri
    }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        companyLogoUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profil użytkownika",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- SEKCJA ZDJĘCIA PROFILOWEGO ---
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                if (profileImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { profilePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DANE UŻYTKOWNIKA (Dynamiczne)
        Text(
            text = userEmail,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isOwner) "Właściciel Kliniki" else "Pracownik",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- SEKCJA: TWOJA FIRMA (DYNAMICZNA) ---
        if (isOwner) {
            Text(
                text = "Twoja Firma",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { logoPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (companyLogoUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(companyLogoUri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            val businessName = tenantData?.name ?: "Brak przypisanej kliniki"
                            val cityName = tenantData?.locations?.firstOrNull()?.addressCity ?: "Skonfiguruj adres"

                            Text(businessName, fontWeight = FontWeight.Bold)
                            Text(cityName, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onEditBusinessClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (tenantData == null) "UTWÓRZ KLINIKĘ" else "ZARZĄDZAJ KLINIKĄ")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- SEKCJA: KONTO ---
        Text(
            text = "Konto i Bezpieczeństwo",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        AccountMenuItem(
            title = "Ustawienia aplikacji",
            icon = Icons.Default.Settings,
            onClick = onSettingsClick
        )

        AccountMenuItem(
            title = "Dane konta",
            icon = Icons.Default.ManageAccounts,
            onClick = { /* Ekran edycji danych */ }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- PRZYCISK WYLOGUJ ---
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("WYLOGUJ SIĘ", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AccountMenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}