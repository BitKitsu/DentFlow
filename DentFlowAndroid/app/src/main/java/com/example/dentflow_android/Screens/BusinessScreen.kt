package com.example.dentflow_android.Screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.dentflow_android.data.remote.AuthViewModel
import com.example.dentflow_android.data.ViewModel.CatalogViewModel
import com.example.dentflow_android.data.ViewModel.FileViewModel
import com.example.dentflow_android.data.ViewModel.PatientViewModel
import com.example.dentflow_android.data.ViewModel.ScheduleViewModel
import com.example.dentflow_android.data.ViewModel.TenantViewModel
import com.example.dentflow_android.data.ViewModel.VisitViewModel

private val CLINIC_NAME_VAL = Regex("^[\\w\\s\\-\\.ąćęłńóśźżĄĆĘŁŃÓŚŹŻ]{2,80}$")

@Composable
fun BusinessScreen(
    onNavigateToSettings: () -> Unit,
    fileViewModel: FileViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel(),
    patientViewModel: PatientViewModel = hiltViewModel(),
    scheduleViewModel: ScheduleViewModel = hiltViewModel(),
    catalogViewModel: CatalogViewModel = hiltViewModel(),
    visitViewModel: VisitViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionState by authViewModel.sessionState.collectAsState()
    val tenantId = sessionState.tenantId

    var showStaffManagement by remember { mutableStateOf(false) }
    var showPatientScreen by remember { mutableStateOf(false) }
    var showScheduleScreen by remember { mutableStateOf(false) }
    var showVisitsScreen by remember { mutableStateOf(false) }
    var showCatalogScreen by remember { mutableStateOf(false) }
    var showEditScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tenantViewModel.loadAllTenantData()
        patientViewModel.loadPatients()
        scheduleViewModel.loadSchedule()
        catalogViewModel.loadServices()
        visitViewModel.refreshVisits()
    }

    val tenantData by tenantViewModel.tenantState
    val patients by patientViewModel.patients.collectAsState()
    val visits by visitViewModel.visits.collectAsState()
    val services by catalogViewModel.servicesState
    val isUploading by fileViewModel.isUploading.collectAsState()

    var logoUrl by remember(tenantData) { mutableStateOf(tenantData?.logoUrl ?: "") }

    val logoCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent ?: return@rememberLauncherForActivityResult
            fileViewModel.uploadImage(
                context = context, tenantId = tenantId, uri = uri,
                onSuccess = { url ->
                    logoUrl = url
                    // Zapisz logo od razu do backendu
                    val t = tenantData
                    if (t != null) {
                        val loc = t.locations?.firstOrNull()
                        tenantViewModel.saveBusinessData(
                            name = t.name,
                            locName = loc?.name ?: "Placówka Główna",
                            street = loc?.addressStreet ?: "",
                            city = loc?.addressCity ?: "",
                            zip = loc?.addressZip ?: "",
                            logoUrl = url
                        )
                    }
                    android.widget.Toast.makeText(context, "Logo zaktualizowane", android.widget.Toast.LENGTH_SHORT).show()
                },
                onError = { msg ->
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    when {
        showStaffManagement -> {
            BackHandler { showStaffManagement = false }
            StaffManagementScreen(onBackClick = { showStaffManagement = false })
        }
        showPatientScreen -> {
            BackHandler { showPatientScreen = false }
            PatientListScreen(onBackClick = { showPatientScreen = false })
        }
        showCatalogScreen -> {
            BackHandler { showCatalogScreen = false }
            CatalogListScreen(onBackClick = { showCatalogScreen = false })
        }
        showVisitsScreen -> {
            BackHandler { showVisitsScreen = false }
            Box(modifier = Modifier.fillMaxSize()) {
                VisitsScreen()
                IconButton(
                    onClick = { showVisitsScreen = false },
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        showScheduleScreen -> {
            BackHandler { showScheduleScreen = false }
            Box(modifier = Modifier.fillMaxSize()) {
                ScheduleScreen(viewModel = scheduleViewModel)
                IconButton(
                    onClick = { showScheduleScreen = false },
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        showEditScreen -> {
            BackHandler { showEditScreen = false }
            EditClinicScreen(
                tenantData = tenantData,
                onBack = { showEditScreen = false },
                tenantViewModel = tenantViewModel
            )
        }
        else -> {
            Column(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Moja Klinika", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth())
                Text(
                    tenantData?.locations?.firstOrNull()?.let { "${it.addressStreet}, ${it.addressCity}" }
                        ?: "Brak skonfigurowanego adresu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Logo
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .clickable {
                                logoCropLauncher.launch(CropImageContractOptions(uri = null,
                                    cropImageOptions = CropImageOptions(imageSourceIncludeGallery = true,
                                        imageSourceIncludeCamera = false,
                                        cropShape = CropImageView.CropShape.RECTANGLE,
                                        outputCompressQuality = 85,
                                        activityBackgroundColor = android.graphics.Color.parseColor("#1E1E1E"))))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoUrl.isNotBlank()) {
                            AsyncImage(model = logoUrl, contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                                Text("Logo", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                if (isUploading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(tenantData?.name ?: "—", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                // Stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BizStatCard(Modifier.weight(1f), "Wizyty", visits.size.toString(), Icons.Default.Event,
                        MaterialTheme.colorScheme.primary) { showVisitsScreen = true }
                    BizStatCard(Modifier.weight(1f), "Pacjenci", patients.size.toString(), Icons.Default.Group,
                        MaterialTheme.colorScheme.secondary) { showPatientScreen = true }
                    BizStatCard(Modifier.weight(1f), "Zabiegi", services.size.toString(), Icons.Default.MedicalServices,
                        MaterialTheme.colorScheme.tertiary) { showCatalogScreen = true }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Menu
                Text("Zarządzanie", modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BizMenuRow("Pracownicy", Icons.Default.Badge) { showStaffManagement = true }
                    BizMenuRow("Pacjenci", Icons.Default.People) { showPatientScreen = true }
                    BizMenuRow("Cennik usług", Icons.Default.Payments) { showCatalogScreen = true }
                    BizMenuRow("Grafik pracy", Icons.Default.CalendarMonth) { showScheduleScreen = true }
                    BizMenuRow("Edytuj dane kliniki", Icons.Default.Edit) { showEditScreen = true }
                }
            }
        }
    }
}

@Composable
fun BizStatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color, onClick: () -> Unit = {}) {
    Card(modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, overflow = TextOverflow.Ellipsis, maxLines = 1)
        }
    }
}

@Composable
fun BizMenuRow(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

// Edit clinic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClinicScreen(
    tenantData: com.example.dentflow_android.data.remote.TenantResponse?,
    onBack: () -> Unit,
    tenantViewModel: TenantViewModel
) {
    val context = LocalContext.current
    val location = tenantData?.locations?.firstOrNull()

    var name by remember { mutableStateOf(tenantData?.name?.takeIf { it != "string" } ?: "") }
    var locName by remember { mutableStateOf(location?.name?.takeIf { it != "string" } ?: "") }
    var street by remember { mutableStateOf(location?.addressStreet?.takeIf { it != "string" } ?: "") }
    var city by remember { mutableStateOf(location?.addressCity?.takeIf { it != "string" } ?: "") }
    var zip by remember { mutableStateOf(location?.addressZip?.replace("-", "")?.takeIf { it != "string" } ?: "") }

    var saveSuccess by remember { mutableStateOf<String?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    val isLoading by tenantViewModel.isLoading

    val isNameValid = CLINIC_NAME_VAL.matches(name)
    val isStreetValid = street.length >= 3
    val isCityValid = city.length >= 2
    val isZipValid = Regex("^[0-9]{5}$").matches(zip)

    val nameError = (showErrors && name.isBlank()) || (name.isNotBlank() && !isNameValid)
    val streetError = (showErrors && street.isBlank()) || (street.isNotBlank() && !isStreetValid)
    val cityError = (showErrors && city.isBlank()) || (city.isNotBlank() && !isCityValid)
    val zipError = (showErrors && zip.isBlank()) || (zip.isNotBlank() && !isZipValid)
    val canSave = isNameValid && isStreetValid && isCityValid && isZipValid

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń klinikę") },
            text = { Text("Czy na pewno chcesz bezpowrotnie usunąć klinikę? Tej operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    tenantViewModel.deleteClinic(
                        onSuccess = { onBack() },
                        onError = { msg -> saveError = msg }
                    )
                }) { Text("Usuń", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edytuj dane kliniki", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .padding(padding).padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Data section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dane kliniki", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            OutlinedTextField(
                value = name, onValueChange = { name = it; saveSuccess = null; saveError = null },
                label = { Text("Nazwa kliniki") },
                leadingIcon = { Icon(Icons.Default.Business, null) },
                isError = nameError,
                supportingText = { if (nameError) Text("Min. 2 znaki", color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
            )

            OutlinedTextField(
                value = locName, onValueChange = { locName = it; saveSuccess = null; saveError = null },
                label = { Text("Nazwa placówki") },
                leadingIcon = { Icon(Icons.Default.Place, null) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
            )

            // Address
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adres kliniki", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            OutlinedTextField(
                value = street, onValueChange = { street = it; saveSuccess = null; saveError = null },
                label = { Text("Ulica i numer") },
                leadingIcon = { Icon(Icons.Default.AddRoad, null) },
                isError = streetError,
                supportingText = { if (streetError) Text("Min. 3 znaki", color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = city, onValueChange = { city = it; saveSuccess = null; saveError = null },
                    label = { Text("Miasto") },
                    isError = cityError,
                    supportingText = { if (cityError) Text("Min. 2 znaki", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.weight(2f), shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = zip,
                    onValueChange = { input -> zip = input.filter { it.isDigit() }.take(5); saveSuccess = null; saveError = null },
                    visualTransformation = { text ->
                        val t = text.text
                        val out = if (t.length >= 2) "${t.take(2)}-${t.drop(2)}" else t
                        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
                            override fun originalToTransformed(offset: Int): Int = if (offset <= 1) offset else if (offset <= 5) offset + 1 else 6
                            override fun transformedToOriginal(offset: Int): Int = if (offset <= 2) offset else if (offset <= 6) offset - 1 else 5
                        }
                        androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(out), offsetMapping)
                    },
                    label = { Text("Kod pocztowy", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    isError = zipError,
                    supportingText = { if (zipError) Text("00-000", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = tfColors, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Feedback
            AnimatedVisibility(visible = saveError != null) {
                val bg = MaterialTheme.colorScheme.errorContainer
                val fg = MaterialTheme.colorScheme.onErrorContainer
                Card(colors = CardDefaults.cardColors(containerColor = bg), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(saveError ?: "", color = fg, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            AnimatedVisibility(visible = saveSuccess != null) {
                val bg = MaterialTheme.colorScheme.primaryContainer
                val fg = MaterialTheme.colorScheme.onPrimaryContainer
                Card(colors = CardDefaults.cardColors(containerColor = bg), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(saveSuccess ?: "", color = fg, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = {
                    if (canSave) {
                        saveSuccess = null; saveError = null
                        tenantViewModel.saveBusinessData(
                            name = name,
                            locName = locName.ifBlank { "Placówka Główna" },
                            street = street,
                            city = city,
                            zip = "${zip.take(2)}-${zip.drop(2)}"
                        )
                        saveSuccess = "Dane zostały zapisane."
                    } else {
                        showErrors = true
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zapisz dane", fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Remove clinic
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Strefa niebezpieczna", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer)
            ) {
                Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Usuń klinikę", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
