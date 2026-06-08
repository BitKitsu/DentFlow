package pl.edu.ur.dentflow

import android.content.SharedPreferences
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
import pl.edu.ur.dentflow.ui.theme.DentFlowAndroidTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import pl.edu.ur.dentflow.Screens.*
import pl.edu.ur.dentflow.data.ViewModel.*
import pl.edu.ur.dentflow.data.remote.AuthViewModel
import javax.inject.Inject

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

                // Sprawdzamy czy użytkownik jest już zalogowany (token w prefs)
                val ctx = androidx.compose.ui.platform.LocalContext.current
                val startPrefs = ctx.getSharedPreferences("dentflow_prefs", android.content.Context.MODE_PRIVATE)
                val savedToken = startPrefs.getString("jwt_token", null)
                val startDestination = if (!savedToken.isNullOrBlank()) "main_dashboard" else "login"

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = startDestination) {
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

                    composable("appointment_setup") {
                        CreateAppointmentScreen(
                            initialDoctorId = "",
                            onSuccess = { navController.popBackStack() }
                        )
                    }

                    composable("appointment_setup/{staffId}") { backStackEntry ->
                        val staffId = backStackEntry.arguments?.getString("staffId") ?: ""
                        CreateAppointmentScreen(
                            initialDoctorId = staffId,
                            onSuccess = { navController.popBackStack() }
                        )
                    }

                    composable("appointment_setup_by_tenant/{tenantId}") { backStackEntry ->
                        val tenantId = backStackEntry.arguments?.getString("tenantId")?.toLongOrNull() ?: -1L
                        CreateAppointmentScreen(
                            initialTenantId = tenantId,
                            onSuccess = { navController.popBackStack() }
                        )
                    }

                    composable("appointment_setup_by_tenant/{tenantId}/{serviceId}") { backStackEntry ->
                        val tenantId = backStackEntry.arguments?.getString("tenantId")?.toLongOrNull() ?: -1L
                        val serviceId = backStackEntry.arguments?.getString("serviceId")?.toLongOrNull() ?: -1L
                        CreateAppointmentScreen(
                            initialTenantId = tenantId,
                            initialServiceId = serviceId,
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

                    composable("account_data") {
                        AccountDataScreen(
                            onBackClick = {
                                navController.navigate("main_dashboard") {
                                    popUpTo("main_dashboard") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("reports") {
                        val visitVm: VisitViewModel = hiltViewModel()
                        ReportsScreen(
                            onBackClick = { navController.popBackStack() },
                            visitViewModel = visitVm
                        )
                    }
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
    catalogViewModel: CatalogViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var isShowingSettings by remember { mutableStateOf(false) }

    val staffList by staffViewModel.staffMembers.collectAsState()
    val allStaff by staffViewModel.allStaff.collectAsState()
    val tenantData by tenantViewModel.tenantState
    val allTenants by tenantViewModel.allTenants
    val serviceList by catalogViewModel.servicesState
    val allCatalog by catalogViewModel.allCatalog
    val sessionState by authViewModel.sessionState.collectAsState()

    LaunchedEffect(tenantData) {
        authViewModel.refreshSession()
    }

    val currentTenant = tenantData
    val userRole = sessionState.role
    val isOwner  = userRole == "OWNER"
    val isDoctor = userRole == "DENTIST"
    val isReceptionist = userRole == "RECEPTIONIST"
    val isAssistant = userRole == "ASSISTANT"
    val isStaff = isOwner || isDoctor || isReceptionist || isAssistant

    val hasClinic = currentTenant != null && currentTenant.id != 0L

    data class NavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tabIndex: Int)
    val navItems = buildList {
        add(NavItem("Oferty", Icons.Default.LocalOffer, 0))
        if (isStaff && hasClinic) add(NavItem("Klinika", Icons.Default.Business, 1))
        add(NavItem("Wizyty", Icons.Default.CalendarMonth, 2))
        add(NavItem("Powiadomienia", Icons.Default.Notifications, 3))
        add(NavItem("Konto", Icons.Default.AccountCircle, 4))
    }

    LaunchedEffect(selectedItem) {
        if (selectedItem == 0) {
            staffViewModel.loadStaff()
            staffViewModel.loadAllStaff()
            tenantViewModel.loadAllTenantData()
            tenantViewModel.loadAllTenants()
            catalogViewModel.loadAllCatalog()
            notificationViewModel.fetchNotifications()
            visitViewModel.refreshVisits()
            catalogViewModel.loadServices()
        }
    }


    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                navItems.forEach { navItem ->
                    NavigationBarItem(
                        icon = {
                            BadgedBox(badge = {
                                if (navItem.tabIndex == 3) {
                                    val unreadCount by notificationViewModel.unreadCount.collectAsState()
                                    if (unreadCount > 0) Badge { Text(unreadCount.toString()) }
                                }
                            }) {
                                Icon(navItem.icon, contentDescription = navItem.label)
                            }
                        },
                        label = { Text(navItem.label, fontSize = 10.sp, maxLines = 1, softWrap = false) },
                        selected = selectedItem == navItem.tabIndex,
                        onClick = {
                            onTabChange(navItem.tabIndex)
                            if (navItem.tabIndex != 4) isShowingSettings = false
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
                    allTenants = allTenants,
                    allCatalog = allCatalog,
                    allStaff = allStaff,
                    onStaffClick = { staff ->
                        navController.navigate("appointment_setup/${staff.id}")
                    },
                    onBookClick = { tenantId, serviceId ->
                        navController.navigate("appointment_setup_by_tenant/$tenantId/$serviceId")
                    }
                )
                1 -> BusinessScreen(
                    onNavigateToSettings = { isShowingSettings = true },
                    onClinicDeleted = {
                        onTabChange(0)
                        tenantViewModel.loadAllTenantData()
                    }
                )
                2 -> VisitsScreen(
                    viewModel = visitViewModel,
                    onCreateClick = { navController.navigate("appointment_setup") }
                )
                3 -> NotificationsScreen(viewModel = notificationViewModel)
                4 -> {
                    if (!isShowingSettings) {
                        AccountScreen(
                            onSettingsClick = { isShowingSettings = true },
                            onLogoutClick = {
                                navController.navigate("login") {
                                    popUpTo("main_dashboard") { inclusive = true }
                                }
                            },
                            onEditBusinessClick = { onTabChange(1) },
                            onAccountDataClick = { navController.navigate("account_data") },
                            onCreateBusinessClick = { navController.navigate("create_tenant_form") },
                            onReportsClick = { navController.navigate("reports") }
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