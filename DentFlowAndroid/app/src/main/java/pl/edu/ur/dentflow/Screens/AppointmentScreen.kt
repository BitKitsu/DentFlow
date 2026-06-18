package pl.edu.ur.dentflow.Screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import pl.edu.ur.dentflow.R
import pl.edu.ur.dentflow.data.ViewModel.*
import pl.edu.ur.dentflow.data.remote.ServiceCatalogItemDTO
import pl.edu.ur.dentflow.data.remote.StaffMemberResponse
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentScreen(
    viewModel: AppointmentViewModel = hiltViewModel(),
    staffViewModel: StaffViewModel = hiltViewModel(),
    patientViewModel: PatientViewModel = hiltViewModel(),
    catalogViewModel: CatalogViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel(),
    authViewModel: pl.edu.ur.dentflow.data.remote.AuthViewModel = hiltViewModel(),
    initialDoctorId: String = "",
    initialTenantId: Long = -1L,
    initialServiceId: Long = -1L,
    onSuccess: () -> Unit
) {
    val allStaff by staffViewModel.allStaff.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val bookingComplete by viewModel.bookingLoadComplete.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val sessionState by authViewModel.sessionState.collectAsState()
    val isPatientRole = sessionState.role == "PATIENT"
    val patients by patientViewModel.patients.collectAsState()

    var currentStep by remember { mutableIntStateOf(1) }
    var selectedDentist by remember { mutableStateOf<StaffMemberResponse?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTimeSlot by remember { mutableStateOf<TimeSlot?>(null) }
    var notes by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf<ServiceCatalogItemDTO?>(null) }
    var selectedTenantId by remember { mutableStateOf<Long?>(if (initialTenantId > 0) initialTenantId else null) }

    val services = catalogViewModel.servicesState.value

    LaunchedEffect(selectedTenantId) {
        selectedTenantId?.let { tid ->
            if (tid > 0) {
                viewModel.syncTenantId(tid)
                catalogViewModel.loadServices()
                staffViewModel.loadAllStaff()
            }
        }
    }

    LaunchedEffect(Unit) {
        staffViewModel.loadAllStaff()
        patientViewModel.loadPatients()
        catalogViewModel.loadServices()
    }

    LaunchedEffect(services, initialServiceId) {
        if (initialServiceId > 0 && selectedService == null) {
            val found = services.find { it.id == initialServiceId }
            if (found != null) {
                selectedService = found
                viewModel.setSlotDuration(found.durationMinutes.toLong())
            }
        }
    }

    LaunchedEffect(allStaff, initialDoctorId) {
        if (initialDoctorId.isNotEmpty() && selectedDentist == null) {
            val docId = initialDoctorId.toLongOrNull()
            val found = allStaff.find { it.id == docId }
            if (found != null) {
                selectedDentist = found
                selectedTenantId = found.tenantId
                currentStep = 2
                viewModel.loadBookingData(found.id, found.tenantId)
            }
        }
    }

    LaunchedEffect(selectedDentist) {
        selectedDentist?.let {
            viewModel.loadBookingData(it.id, it.tenantId)
        }
    }

    val patientId = remember(patients, isPatientRole, sessionState) {
        if (isPatientRole) {
            patients.find { it.userId == sessionState.userId }?.id
        } else null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentStep) {
                            1 -> stringResource(R.string.appointment_step_doctor)
                            2 -> stringResource(R.string.appointment_step_date)
                            3 -> stringResource(R.string.appointment_step_confirm)
                            else -> ""
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (currentStep) {
                            1 -> onSuccess()
                            2 -> {
                                selectedDentist = null
                                viewModel.resetBookingState()
                                currentStep = 1
                            }
                            3 -> {
                                selectedDate = null
                                selectedTimeSlot = null
                                notes = ""
                                selectedService = null
                                currentStep = 2
                            }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            StepIndicator(currentStep)

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { if (targetState > initialState) it else -it } togetherWith
                            fadeOut() + slideOutHorizontally { if (targetState > initialState) -it else it }
                },
                label = "step"
            ) { step ->
                when (step) {
                    1 -> Step1ChooseDentist(
                        staff = allStaff,
                        tenantId = selectedTenantId,
                        selectedDentist = selectedDentist,
                        onDentistSelected = { dentist ->
                            selectedDentist = dentist
                            currentStep = 2
                        }
                    )
                    2 -> Step2PickDate(
                        viewModel = viewModel,
                        dentist = selectedDentist,
                        selectedDate = selectedDate,
                        onDateSelected = { date ->
                            selectedDate = date
                            val duration = selectedService?.durationMinutes?.toLong() ?: 30
                            viewModel.computeAvailableSlotsForDate(date, duration)
                            currentStep = 3
                        }
                    )
                    3 -> Step3PickTime(
                        viewModel = viewModel,
                        dentist = selectedDentist,
                        date = selectedDate,
                        selectedTimeSlot = selectedTimeSlot,
                        notes = notes,
                        services = services,
                        selectedService = selectedService,
                        servicePreselected = initialServiceId > 0,
                        initialServiceId = initialServiceId,
                        onNotesChange = { notes = it },
                        onServiceSelected = { service ->
                            selectedService = service
                            viewModel.setSlotDuration(service.durationMinutes.toLong())
                            selectedTimeSlot = null
                            selectedDate?.let { date ->
                                viewModel.computeAvailableSlotsForDate(date, service.durationMinutes.toLong())
                            }
                        },
                        onTimeSelected = { slot -> selectedTimeSlot = slot },
                        onConfirm = {
                            val slot = selectedTimeSlot
                            val dentist = selectedDentist
                            val date = selectedDate
                            if (slot != null && dentist != null && date != null) {
                                viewModel.createAppointment(
                                    locId = slot.locationId,
                                    room = slot.roomId,
                                    docId = dentist.id,
                                    patId = patientId,
                                    servId = selectedService?.id,
                                    start = slot.startIso,
                                    end = slot.endIso,
                                    note = notes,
                                    tenantId = selectedTenantId ?: -1L,
                                    onSuccess = {
                                        viewModel.resetBookingState()
                                        onSuccess()
                                    }
                                )
                            }
                        },
                        isLoading = isLoading || isCreating
                    )
                }
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(1, 2, 3).forEach { step ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (step <= currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (step < currentStep) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = step.toString(),
                            color = if (step <= currentStep) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (step) {
                        1 -> stringResource(R.string.appointment_label_doctor)
                        2 -> stringResource(R.string.appointment_label_date)
                        3 -> stringResource(R.string.appointment_label_time)
                        else -> ""
                    },
                    fontSize = 11.sp,
                    color = if (step <= currentStep) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (step < 3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 8.dp)
                        .background(
                            if (step < currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
fun Step1ChooseDentist(
    staff: List<StaffMemberResponse>,
    tenantId: Long?,
    selectedDentist: StaffMemberResponse?,
    onDentistSelected: (StaffMemberResponse) -> Unit
) {
                val dentists = staff.filter {
                    val role = it.profession.uppercase()
                    role == "DENTIST" && (tenantId == null || it.tenantId == tenantId)
                }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.appointment_select_doctor),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (dentists.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                dentists.forEach { member ->
                    DentistCard(
                        dentist = member,
                        isSelected = selectedDentist?.id == member.id,
                        onClick = { onDentistSelected(member) }
                    )
                }
            }
        }
    }
}

@Composable
fun DentistCard(
    dentist: StaffMemberResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (!dentist.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = dentist.avatarUrl,
                        contentDescription = stringResource(R.string.doctor_format, dentist.firstName, dentist.lastName),
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.doctor_format, dentist.firstName, dentist.lastName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dentist.profession,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun Step2PickDate(
    viewModel: AppointmentViewModel,
    dentist: StaffMemberResponse?,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val availableDates by viewModel.availableDates.collectAsState()
    val bookingLoadComplete by viewModel.bookingLoadComplete.collectAsState()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        dentist?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.doctor_format, it.firstName, it.lastName),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, stringResource(R.string.appointment_prev_month))
            }
            Text(
                text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("pl"))
                    .replaceFirstChar { it.uppercase() } + " " + currentMonth.year,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, stringResource(R.string.appointment_next_month))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(
                stringResource(R.string.day_mon_short),
                stringResource(R.string.day_tue_short),
                stringResource(R.string.day_wed_short),
                stringResource(R.string.day_thu_short),
                stringResource(R.string.day_fri_short),
                stringResource(R.string.day_sat_short),
                stringResource(R.string.day_sun_short)
            ).forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (!bookingLoadComplete) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            CalendarGrid(
                yearMonth = currentMonth,
                availableDates = availableDates,
                onDateSelected = onDateSelected
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.appointment_available), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.appointment_unavailable), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    availableDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    val totalDays = lastDayOfMonth.dayOfMonth
    val today = LocalDate.now()

    val totalCells = ((firstDayOfWeek - 1) + totalDays + 6) / 7 * 7

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.heightIn(max = 280.dp),
        userScrollEnabled = false
    ) {
        items(totalCells) { index ->
            val dayOffset = index - (firstDayOfWeek - 1)
            val day = dayOffset + 1

            if (dayOffset < 0 || day > totalDays) {
                Box(modifier = Modifier.aspectRatio(1f))
            } else {
                val date = yearMonth.atDay(day)
                val isAvailable = date in availableDates
                val isPast = date.isBefore(today)
                val isToday = date == today

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .then(
                            when {
                                isAvailable && !isPast -> Modifier
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { onDateSelected(date) }
                                isToday -> Modifier
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else -> Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        color = when {
                            isAvailable && !isPast -> MaterialTheme.colorScheme.onPrimary
                            isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontSize = 13.sp,
                        fontWeight = if (isAvailable && !isPast) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3PickTime(
    viewModel: AppointmentViewModel,
    dentist: StaffMemberResponse?,
    date: LocalDate?,
    selectedTimeSlot: TimeSlot?,
    notes: String,
    services: List<ServiceCatalogItemDTO>,
    selectedService: ServiceCatalogItemDTO?,
    servicePreselected: Boolean,
    initialServiceId: Long = -1L,
    onNotesChange: (String) -> Unit,
    onServiceSelected: (ServiceCatalogItemDTO) -> Unit,
    onTimeSelected: (TimeSlot) -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean
) {
    val availableSlots by viewModel.availableSlotsForDate.collectAsState()
    var serviceMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        dentist?.let {
            Text(
                text = stringResource(R.string.doctor_format, it.firstName, it.lastName),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        date?.let {
            Text(
                text = it.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("pl")))
                    .replaceFirstChar { c -> c.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.appointment_available_hours),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (availableSlots.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = stringResource(R.string.appointment_no_hours),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                availableSlots.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { slot ->
                            val isSelected = selectedTimeSlot?.startIso == slot.startIso
                            val timeText = "${slot.time.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${slot.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onTimeSelected(slot) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) null else CardDefaults.outlinedCardBorder()
                            ) {
                                Text(
                                    text = timeText,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (servicePreselected) {
            val service = selectedService ?: services.find { it.id == initialServiceId }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (service != null) stringResource(R.string.service_format, service.name, service.durationMinutes, "%.2f".format(service.priceCents / 100.0))
                               else "Usługa #$initialServiceId",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.appointment_service_optional),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = serviceMenuExpanded,
                onExpandedChange = { serviceMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedService?.let { stringResource(R.string.service_format, it.name, it.durationMinutes, "%.2f".format(it.priceCents / 100.0)) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.appointment_select_service)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = serviceMenuExpanded,
                    onDismissRequest = { serviceMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.appointment_no_service)) },
                        onClick = {
                            onServiceSelected(ServiceCatalogItemDTO(
                                id = 0L, tenantId = 0L, name = "",
                                durationMinutes = 30, priceCents = 0, active = true
                            ))
                            serviceMenuExpanded = false
                        }
                    )
                    services.filter { it.active }.forEach { service ->
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.service_format, service.name, service.durationMinutes, "%.2f".format(service.priceCents / 100.0)))
                            },
                            onClick = {
                                onServiceSelected(service)
                                serviceMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.appointment_notes_optional)) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isLoading && selectedTimeSlot != null
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(Icons.Default.Check, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.appointment_confirm), fontWeight = FontWeight.Bold)
            }
        }
    }
}
