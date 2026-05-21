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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dentflow_android.ui.theme.DentFlowAndroidTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.Screens.*
import com.example.dentflow_android.data.ViewModel.*

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
                val tenantViewModel: TenantViewModel = hiltViewModel()
                var currentDashboardTab by remember { mutableIntStateOf(0) }

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                currentDashboardTab = 0
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
                            navController = navController,
                            tenantViewModel = tenantViewModel,
                            selectedItem = currentDashboardTab,
                            onTabChange = { currentDashboardTab = it }
                        )
                    }

                    composable("staff_management") {
                        StaffManagementScreen(onBackClick = { navController.popBackStack() })
                    }

                    composable("patient_list") {
                        PatientListScreen(onBackClick = { navController.popBackStack() })
                    }

                    composable("catalog_management") {
                        CatalogListScreen(onBackClick = { navController.popBackStack() })
                    }

                    composable("create_tenant_form") {
                        CreateTenantScreen(
                            onBack = { navController.popBackStack() },
                            tenantViewModel = tenantViewModel
                        )
                    }

                    composable("appointment_setup/{staffId}") { backStackEntry ->
                        val staffId = backStackEntry.arguments?.getString("staffId") ?: ""
                        CreateAppointmentScreen(
                            initialDoctorId = staffId,
                            onSuccess = { navController.popBackStack() }
                        )
                    }

                    composable("schedule") {
                        ScheduleScreen()
                    }

                    composable("settings") {
                        SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { isDarkTheme = it },
                            onBackClick = { navController.popBackStack() }
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
    navController: NavHostController,
    selectedItem: Int,
    onTabChange: (Int) -> Unit,
    staffViewModel: StaffViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    visitViewModel: VisitViewModel = hiltViewModel(),
    catalogViewModel: CatalogViewModel = hiltViewModel()
) {
    var isShowingSettings by remember { mutableStateOf(false) }

    val staffList by staffViewModel.staffMembers.collectAsState()
    val tenantData by tenantViewModel.tenantState
    val serviceList by catalogViewModel.servicesState

    val currentTenant = tenantData

    LaunchedEffect(Unit) {
        staffViewModel.loadStaff()
        tenantViewModel.loadAllTenantData()
        notificationViewModel.fetchNotifications()
        visitViewModel.refreshVisits()
        catalogViewModel.loadServices()
    }

    val items = listOf("Home", "Firma", "Admin", "Wizyty", "Alarmy", "Konto")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Business,
        Icons.Default.AdminPanelSettings,
        Icons.Default.CalendarMonth,
        Icons.Default.Notifications,
        Icons.Default.AccountCircle
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            BadgedBox(badge = {
                                if (index == 4) {
                                    val unreadCount by notificationViewModel.unreadCount.collectAsState()
                                    if (unreadCount > 0) Badge { Text(unreadCount.toString()) }
                                }
                            }) {
                                Icon(icons[index], contentDescription = item)
                            }
                        },
                        label = { Text(item, fontSize = 10.sp) },
                        selected = selectedItem == index,
                        onClick = {
                            onTabChange(index)
                            if (index != 5) isShowingSettings = false
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> HomeScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    staffList = staffList,
                    serviceList = serviceList,
                    tenantData = tenantData,
                    onStaffClick = { staff ->
                        navController.navigate("appointment_setup/${staff.id}")
                    }
                )
                1 -> {
                    if (currentTenant == null || currentTenant.id == 0L) {
                        EmptyTenantView(
                            onCreateClick = { navController.navigate("create_tenant_form") }
                        )
                    } else {
                        BusinessScreen()
                    }
                }
                2 -> AdminPanelScreen(
                    onNavigateToStaff    = { navController.navigate("staff_management") },
                    onNavigateToPatients = { navController.navigate("patient_list") },
                    onNavigateToCatalog  = { navController.navigate("catalog_management") },
                    onNavigateToSchedule = { navController.navigate("schedule") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
                3 -> VisitsScreen(viewModel = visitViewModel)
                4 -> NotificationsScreen(viewModel = notificationViewModel)
                5 -> {
                    if (!isShowingSettings) {
                        AccountScreen(
                            onSettingsClick = { isShowingSettings = true },
                            onLogoutClick = {
                                navController.navigate("login") {
                                    popUpTo("main_dashboard") { inclusive = true }
                                }
                            },
                            onEditBusinessClick = { onTabChange(1) }
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

@Composable
fun EmptyTenantView(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AddBusiness,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Brak aktywnej kliniki",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onCreateClick,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Utwórz nową klinikę")
        }
    }
}