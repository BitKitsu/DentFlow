package com.example.dentflow_android.Screens

import android.net.Uri
import android.content.SharedPreferences
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.dentflow_android.data.ViewModel.FileViewModel
import com.example.dentflow_android.data.ViewModel.TenantViewModel
import com.example.dentflow_android.data.remote.AuthViewModel

@Composable
fun AccountScreen(
    tenantViewModel: TenantViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    fileViewModel: FileViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onEditBusinessClick: () -> Unit,
    onAccountDataClick: () -> Unit,
    onCreateBusinessClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val sessionState by authViewModel.sessionState.collectAsState()
    val userEmail = sessionState.email.takeIf { it.isNotBlank() } ?: "uzytkownik@dentflow.pl"
    val userRole = sessionState.role
    val isOwner = userRole == "OWNER"
    val avatarUrl = sessionState.avatarUrl
    val tenantId = sessionState.tenantId

    // Read data
    LaunchedEffect(Unit) {
        tenantViewModel.loadAllTenantData()
    }

    val tenantData by tenantViewModel.tenantState
    val isUploading by fileViewModel.isUploading.collectAsState()
    // Clonic logo downloaded from tenantState
    var logoUrl by remember(tenantData) { mutableStateOf(tenantData?.logoUrl ?: "") }

    // Avatar cropper (round 1:1)
    val avatarCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent ?: return@rememberLauncherForActivityResult
            fileViewModel.uploadImage(
                context = context, tenantId = tenantId, uri = uri,
                onSuccess = { url ->
                    authViewModel.updateProfile(
                        firstName = null, lastName = null, phone = null,
                        email = null, addressStreet = null, addressCity = null,
                        addressZip = null, addressCountry = null, avatarUrl = url,
                        onSuccess = {},
                        onError = {}
                    )
                },
                onError = { msg ->
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            )
        }
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

        // Avatar
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                if (avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = avatarUrl,
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
                    .clickable {
                        avatarCropLauncher.launch(
                            CropImageContractOptions(
                                uri = null,
                                cropImageOptions = CropImageOptions(
                                    imageSourceIncludeGallery = true,
                                    imageSourceIncludeCamera = true,
                                    cropShape = CropImageView.CropShape.OVAL,
                                    fixAspectRatio = true,
                                    aspectRatioX = 1, aspectRatioY = 1,
                                    outputCompressQuality = 85,
                                    activityBackgroundColor = android.graphics.Color.parseColor("#1E1E1E")
                                )
                            )
                        )
                    },
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
        if (isUploading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User data
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

        // Clinic
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
                    if (tenantData == null || tenantData?.id == 0L) {
                        // Creating new clinic
                        Text(
                            "Brak przypisanej kliniki",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Utwórz profil firmy, aby rozpocząć konfigurację kalendarza i dodawanie personelu.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                        Button(
                            onClick = onCreateBusinessClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("UTWÓRZ KLINIKĘ")
                        }
                    } else {
                        // Managing existing clinic
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = logoUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                val businessName = tenantData?.name ?: "Brak nazwy"
                                val cityName = tenantData?.locations?.firstOrNull()?.addressCity ?: "Brak adresu"

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
                            Text("ZARZĄDZAJ KLINIKĄ")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Account
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

        var showDeleteDialog by remember { mutableStateOf(false) }

        AccountMenuItem(
            title = "Dane konta",
            icon = Icons.Default.ManageAccounts,
            onClick = onAccountDataClick
        )
        
        AccountMenuItem(
            title = "Usuń konto",
            icon = Icons.Default.DeleteForever,
            onClick = { showDeleteDialog = true }
        )

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Usuń konto") },
                text = { Text("Czy na pewno chcesz bezpowrotnie usunąć swoje konto? Tej operacji nie można cofnąć.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            authViewModel.deleteAccount(
                                onSuccess = {
                                    authViewModel.logout()
                                    onLogoutClick()
                                },
                                onError = { errorMsg ->
                                    android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    ) {
                        Text("Usuń", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        // Logout
        Button(
            onClick = {
                authViewModel.logout()
                onLogoutClick()
            },
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
