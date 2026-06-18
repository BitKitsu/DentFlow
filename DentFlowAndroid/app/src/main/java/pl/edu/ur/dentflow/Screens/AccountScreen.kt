package pl.edu.ur.dentflow.Screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import pl.edu.ur.dentflow.R
import pl.edu.ur.dentflow.data.ViewModel.FileViewModel
import pl.edu.ur.dentflow.data.ViewModel.TenantViewModel
import pl.edu.ur.dentflow.data.remote.AuthViewModel

@Composable
fun AccountScreen(
    tenantViewModel: TenantViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    fileViewModel: FileViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onEditBusinessClick: () -> Unit,
    onAccountDataClick: () -> Unit,
    onCreateBusinessClick: () -> Unit,
    onReportsClick: () -> Unit = {}
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
            text = stringResource(R.string.account_title),
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
            text = when (userRole) {
                "OWNER" -> stringResource(R.string.role_owner)
                "DENTIST" -> stringResource(R.string.role_dentist)
                "RECEPTIONIST" -> stringResource(R.string.role_receptionist)
                "ASSISTANT" -> stringResource(R.string.role_assistant)
                "PATIENT" -> stringResource(R.string.role_patient)
                else -> stringResource(R.string.role_staff)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Clinic
        Text(
            text = stringResource(R.string.account_your_company),
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
                        stringResource(R.string.account_no_clinic),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.account_no_clinic_desc),
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
                        Text(stringResource(R.string.account_create_clinic))
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
                            val businessName = tenantData?.name ?: stringResource(R.string.account_no_name)
                            val cityName = tenantData?.locations?.firstOrNull()?.addressCity ?: stringResource(R.string.account_no_address)

                            Text(businessName, fontWeight = FontWeight.Bold)
                            Text(cityName, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isOwner) {
                        Button(
                            onClick = onEditBusinessClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.account_manage_clinic))
                        }
                    }

                    if (isOwner) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onReportsClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.account_reports_pdf))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Account
        Text(
            text = stringResource(R.string.account_security),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        AccountMenuItem(
            title = stringResource(R.string.account_settings),
            icon = Icons.Default.Settings,
            onClick = onSettingsClick
        )

        var showDeleteDialog by remember { mutableStateOf(false) }

        AccountMenuItem(
            title = stringResource(R.string.account_data),
            icon = Icons.Default.ManageAccounts,
            onClick = onAccountDataClick
        )
        
        AccountMenuItem(
            title = stringResource(R.string.account_delete),
            icon = Icons.Default.DeleteForever,
            onClick = { showDeleteDialog = true }
        )

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.account_delete_title)) },
                text = { Text(stringResource(R.string.account_delete_text)) },
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
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.cancel))
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
            Text(stringResource(R.string.account_logout), fontWeight = FontWeight.Bold)
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
