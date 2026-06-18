package pl.edu.ur.dentflow.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.edu.ur.dentflow.data.ViewModel.TenantViewModel
import pl.edu.ur.dentflow.utils.ValidationUtils
import pl.edu.ur.dentflow.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext

private val CLINIC_NAME_REGEX = Regex("^[\\w\\s\\-\\.ąćęłńóśźżĄĆĘŁŃÓŚŹŻ]{2,80}$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTenantScreen(
    onBack: () -> Unit,
    tenantViewModel: TenantViewModel
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    // ZIP code: 5 digits with dash
    var zip by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Polska") }

    var showErrors by remember { mutableStateOf(false) }

    val isLoading by tenantViewModel.isLoading
    val tenantState by tenantViewModel.tenantState

    LaunchedEffect(tenantState) {
        if (tenantState != null) {
            onBack()
        }
    }

    val isNameValid = CLINIC_NAME_REGEX.matches(name)
    val isLocationNameValid = locationName.isBlank() || locationName.length >= 2
    val isStreetValid = street.length >= 3
    val isCityValid = city.length >= 2
    val isZipValid = ValidationUtils.isZipValid(zip)
    val isCountryValid = country.length >= 2

    val nameError = (showErrors && name.isBlank()) || (name.isNotBlank() && !isNameValid)
    val locationNameError = locationName.isNotBlank() && !isLocationNameValid
    val streetError = (showErrors && street.isBlank()) || (street.isNotBlank() && !isStreetValid)
    val cityError = (showErrors && city.isBlank()) || (city.isNotBlank() && !isCityValid)
    val zipError = (showErrors && zip.isBlank()) || (zip.isNotBlank() && !isZipValid)

    val canSubmit = isNameValid && isLocationNameValid && isStreetValid && isCityValid && isZipValid && isCountryValid

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tenant_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.tenant_basic_data),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; showErrors = false },
                label = { Text(stringResource(R.string.tenant_clinic_name)) },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                isError = nameError,
                supportingText = {
                    if (nameError) Text(stringResource(R.string.min_2_chars), color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it; showErrors = false },
                label = { Text(stringResource(R.string.tenant_location_name)) },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true,
                isError = locationNameError,
                supportingText = { if (locationNameError) Text(stringResource(R.string.tenant_location_name_error)) }
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.tenant_address),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = street,
                onValueChange = { street = it; showErrors = false },
                label = { Text(stringResource(R.string.tenant_street)) },
                leadingIcon = { Icon(Icons.Default.AddRoad, contentDescription = null) },
                isError = streetError,
                supportingText = {
                    if (streetError) Text(stringResource(R.string.min_3_chars), color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it; showErrors = false },
                    label = { Text(stringResource(R.string.tenant_city)) },
                    isError = cityError,
                    supportingText = {
                        if (cityError) Text(stringResource(R.string.min_2_chars), color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = zip,
                    onValueChange = { input ->
                        zip = input.filter { it.isDigit() }.take(5)
                        showErrors = false
                    },
                    visualTransformation = { text ->
                        val t = text.text
                        val out = if (t.length >= 2) "${t.take(2)}-${t.drop(2)}" else t
                        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
                            override fun originalToTransformed(offset: Int): Int = if (offset <= 1) offset else if (offset <= 5) offset + 1 else 6
                            override fun transformedToOriginal(offset: Int): Int = if (offset <= 2) offset else if (offset <= 6) offset - 1 else 5
                        }
                        androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(out), offsetMapping)
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.tenant_zip),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    isError = zipError,
                    supportingText = {
                        if (zipError) Text(stringResource(R.string.postal_code_hint), color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            OutlinedTextField(
                value = country,
                onValueChange = { country = it; showErrors = false },
                label = { Text(stringResource(R.string.tenant_country)) },
                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                isError = showErrors && !isCountryValid,
                supportingText = {
                    if (showErrors && !isCountryValid) Text(stringResource(R.string.min_2_chars), color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (canSubmit) {
                        tenantViewModel.registerClinic(
                            name = name,
                            locationName = locationName.ifBlank { context.getString(R.string.tenant_main_location) },
                            street = street,
                            city = city,
                            zip = "${zip.take(2)}-${zip.drop(2)}",
                            country = country
                        )
                    } else {
                        showErrors = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.AddBusiness, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.tenant_create), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
