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

private val CLINIC_NAME_REGEX = Regex("^[\\w\\s\\-\\.ąćęłńóśźżĄĆĘŁŃÓŚŹŻ]{2,80}$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTenantScreen(
    onBack: () -> Unit,
    tenantViewModel: TenantViewModel
) {
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
    val isStreetValid = street.length >= 3
    val isCityValid = city.length >= 2
    val isZipValid = Regex("^[0-9]{5}$").matches(zip)
    val isCountryValid = country.length >= 2

    val nameError = (showErrors && name.isBlank()) || (name.isNotBlank() && !isNameValid)
    val streetError = (showErrors && street.isBlank()) || (street.isNotBlank() && !isStreetValid)
    val cityError = (showErrors && city.isBlank()) || (city.isNotBlank() && !isCityValid)
    val zipError = (showErrors && zip.isBlank()) || (zip.isNotBlank() && !isZipValid)

    val canSubmit = isNameValid && isStreetValid && isCityValid && isZipValid && isCountryValid

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zarejestruj Klinikę") },
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
                text = "Dane podstawowe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; showErrors = false },
                label = { Text("Nazwa kliniki") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                isError = nameError,
                supportingText = {
                    if (nameError) Text("Min. 2 znaki", color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it; showErrors = false },
                label = { Text("Nazwa placówki (np. Gabinet Główny)") },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Adres",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = street,
                onValueChange = { street = it; showErrors = false },
                label = { Text("Ulica i numer") },
                leadingIcon = { Icon(Icons.Default.AddRoad, contentDescription = null) },
                isError = streetError,
                supportingText = {
                    if (streetError) Text("Min. 3 znaki", color = MaterialTheme.colorScheme.error)
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
                    label = { Text("Miasto") },
                    isError = cityError,
                    supportingText = {
                        if (cityError) Text("Min. 2 znaki", color = MaterialTheme.colorScheme.error)
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
                            override fun originalToTransformed(offset: Int): Int {
                                if (offset <= 1) return offset
                                if (offset <= 5) return offset + 1
                                return 6
                            }
                            override fun transformedToOriginal(offset: Int): Int {
                                if (offset <= 2) return offset
                                if (offset <= 6) return offset - 1
                                return 5
                            }
                        }
                        androidx.compose.ui.text.input.TransformedText(
                            androidx.compose.ui.text.AnnotatedString(out), offsetMapping
                        )
                    },
                    label = {
                        Text(
                            text = "Kod pocztowy",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    isError = zipError,
                    supportingText = {
                        if (zipError) Text("00-000", color = MaterialTheme.colorScheme.error)
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
                label = { Text("Kraj") },
                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                isError = showErrors && !isCountryValid,
                supportingText = {
                    if (showErrors && !isCountryValid) Text("Min. 2 znaki", color = MaterialTheme.colorScheme.error)
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
                        val formattedZip = "${zip.take(2)}-${zip.drop(2)}"
                        tenantViewModel.registerClinic(
                            name = name,
                            locationName = locationName.ifBlank { "Placówka Główna" },
                            street = street,
                            city = city,
                            zip = formattedZip,
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
                    Text("UTWÓRZ I ZAREJESTRUJ", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
