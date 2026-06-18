package pl.edu.ur.dentflow.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.dentflow.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    themeMode: String,
    onThemeChange: (String) -> Unit,
    authUrl: String,
    coreUrl: String,
    onAuthUrlChange: (String) -> Unit,
    onCoreUrlChange: (String) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.settings_theme),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                var themeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = themeExpanded,
                    onExpandedChange = { themeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .menuAnchor(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = when (themeMode) {
                                "dark" -> stringResource(R.string.settings_theme_dark)
                                "light" -> stringResource(R.string.settings_theme_light)
                                else -> stringResource(R.string.settings_theme_system)
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded)
                    }
                    ExposedDropdownMenu(
                        expanded = themeExpanded,
                        onDismissRequest = { themeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_theme_system)) },
                            onClick = { onThemeChange("system"); themeExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_theme_light)) },
                            onClick = { onThemeChange("light"); themeExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_theme_dark)) },
                            onClick = { onThemeChange("dark"); themeExpanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.settings_language),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            var languageExpanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                ExposedDropdownMenuBox(
                    expanded = languageExpanded,
                    onExpandedChange = { languageExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .menuAnchor(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == "pl") stringResource(R.string.settings_language_polish)
                                       else stringResource(R.string.settings_language_english),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded)
                    }
                    ExposedDropdownMenu(
                        expanded = languageExpanded,
                        onDismissRequest = { languageExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_language_polish)) },
                            onClick = {
                                onLanguageChange("pl")
                                languageExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_language_english)) },
                            onClick = {
                                onLanguageChange("en")
                                languageExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.settings_servers),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.settings_api_config),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    var authUrlInput by remember(authUrl) { mutableStateOf(authUrl) }
                    var coreUrlInput by remember(coreUrl) { mutableStateOf(coreUrl) }

                    fun normalizeUrl(url: String): String {
                        val trimmed = url.trim()
                        if (trimmed.isBlank()) return trimmed
                        return if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                            "http://$trimmed"
                        } else trimmed
                    }

                    OutlinedTextField(
                        value = authUrlInput,
                        onValueChange = { authUrlInput = it },
                        label = { Text(stringResource(R.string.settings_auth_url)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = coreUrlInput,
                        onValueChange = { coreUrlInput = it },
                        label = { Text(stringResource(R.string.settings_core_url)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onAuthUrlChange(normalizeUrl(authUrlInput))
                            onCoreUrlChange(normalizeUrl(coreUrlInput))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.settings_save_urls), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
