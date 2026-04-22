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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dentflow_android.ui.theme.DentFlowAndroidTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.Screens.AccountScreen
import com.example.dentflow_android.Screens.AdminPanelScreen
import com.example.dentflow_android.Screens.BusinessScreen
import com.example.dentflow_android.Screens.HomeScreen
import com.example.dentflow_android.Screens.LoginScreen
import com.example.dentflow_android.Screens.NotificationsScreen
import com.example.dentflow_android.Screens.PatientListScreen
import com.example.dentflow_android.Screens.RegisterScreen
import com.example.dentflow_android.Screens.SettingsScreen
import com.example.dentflow_android.Screens.StaffManagementScreen
import com.example.dentflow_android.Screens.VisitListScreen

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
                            onLoginSuccess = { tenantId ->
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
                            navController = navController
                        )
                    }

                    composable("staff_management") {
                        StaffManagementScreen(onBackClick = { navController.popBackStack() })
                    }
                    composable("patient_list") {
                        PatientListScreen(onBackClick = { navController.popBackStack() })
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
    navController: NavHostController
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    var isShowingSettings by remember { mutableStateOf(false) }

    val items = listOf("Home", "Firma", "Admin", "Wizyty", "Alarmy", "Konto")
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
                tonalElevation = 0.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, fontSize = 10.sp) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            // Resetuj widok ustawień przy zmianie zakładki
                            if (index != 5) isShowingSettings = false
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> HomeScreen(isDarkTheme = isDarkTheme, onThemeChange = onThemeChange)
                1 -> BusinessScreen()
                2 -> AdminPanelScreen(
                    onNavigateToStaff = { navController.navigate("staff_management") },
                    onNavigateToPatients = { navController.navigate("patient_list") }
                )
                3 -> VisitListScreen(viewModel = hiltViewModel())
                4 -> NotificationsScreen()
                5 -> {
                    if (!isShowingSettings) {
                        // --- POPRAWIONE WYWOŁANIE AccountScreen ---
                        AccountScreen(
                            onSettingsClick = { isShowingSettings = true },
                            onLogoutClick = {
                                // Akcja wylogowania: powrót do ekranu logowania
                                navController.navigate("login") {
                                    popUpTo("main_dashboard") { inclusive = true }
                                }
                            },
                            onEditBusinessClick = {
                                // Przekierowanie do zakładki "Firma" (index 1)
                                selectedItem = 1
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
