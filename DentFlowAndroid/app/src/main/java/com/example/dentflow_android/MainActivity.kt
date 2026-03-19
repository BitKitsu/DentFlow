package com.example.dentflow_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dentflow_android.ui.theme.DentFlowAndroidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemInDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemInDark) }

            DentFlowAndroidTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { role ->
                                navController.navigate("main_dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onRegisterClick = { navController.navigate("register") }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = { navController.navigate("login") },
                            onBackToLogin = { navController.popBackStack() }
                        )
                    }

                    composable("main_dashboard") {
                        MainDashboard(
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { isDarkTheme = it },
                            navController = navController // Przekazujemy kontroler nawigacji
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainDashboard(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    navController: NavHostController // Dodany parametr
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    var isShowingSettings by remember { mutableStateOf(false) }

    val items = listOf("Home", "Twoja Firma", "Admin panel", "Wizyty", "Powiadomienia", "Konto")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Business,
        Icons.Default.AdminPanelSettings,
        Icons.Default.EventNote,
        Icons.Default.Notifications,
        Icons.Default.AccountCircle
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.height(130.dp)
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = item,
                                modifier = Modifier.size(25.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item,
                                fontSize = 10.sp,
                                maxLines = 2,
                                textAlign = TextAlign.Center
                            )
                        },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            // Resetujemy widok ustawień przy zmianie zakładki
                            if (index != 5) isShowingSettings = false
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            var hasBusiness by remember { mutableStateOf(false) }

            when (selectedItem) {
                0 -> HomeScreen(isDarkTheme = isDarkTheme, onThemeChange = onThemeChange)

                1 -> {
                    if (!hasBusiness) {
                        CreateBusinessScreen(onBusinessCreated = { hasBusiness = true })
                    } else {
                        BusinessScreen()
                    }
                }

                2 -> AdminPanelScreen()

                3 -> VisitsScreen(userRole = "OWNER")

                4 -> NotificationsScreen()

                5 -> {
                    if (!isShowingSettings) {
                        AccountScreen(
                            isOwner = true,
                            onSettingsClick = { isShowingSettings = true },
                            onLogoutClick = {
                                navController.navigate("login") {
                                    popUpTo("main_dashboard") { inclusive = true }
                                }
                            }
                        )
                    } else {
                        SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onThemeChange = onThemeChange,
                            onBackClick = { isShowingSettings = false }
                        )
                    }
                }
            }
        }
    }
}