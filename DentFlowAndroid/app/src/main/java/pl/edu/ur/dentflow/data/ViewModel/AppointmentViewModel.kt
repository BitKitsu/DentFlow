package pl.edu.ur.dentflow.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.edu.ur.dentflow.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val apiService: ApiService,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<AppointmentResponse>>(emptyList())
    val appointments: StateFlow<List<AppointmentResponse>> = _appointments

    private val _selectedAppointment = MutableStateFlow<AppointmentResponse?>(null)
    val selectedAppointment: StateFlow<AppointmentResponse?> = _selectedAppointment

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val TAG = "AppointmentViewModel"

    private val currentTenantId: Long
        get() = prefs.getLong("tenant_id", -1L)

    private val currentUserId: Long
        get() = prefs.getLong("user_id", -1L)

    private val userRole: String
        get() = prefs.getString("user_role", "PATIENT") ?: "PATIENT"

    // ── Booking wizard state ─────────────────────────────────────────────────

    private val _bookingAppointments = MutableStateFlow<List<AppointmentResponse>>(emptyList())
    val bookingAppointments: StateFlow<List<AppointmentResponse>> = _bookingAppointments

    private val _bookingBlockers = MutableStateFlow<List<ScheduleBlockerDTO>>(emptyList())
    val bookingBlockers: StateFlow<List<ScheduleBlockerDTO>> = _bookingBlockers

    private var _bookingWorkingHours = listOf<StaffWorkingHoursDTO>()

    private var _defaultLocationId = 0L

    private var _defaultRoomId: Long? = null

    private var _slotDurationMinutes: Long = 30

    private val _availableDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    val availableDates: StateFlow<Set<LocalDate>> = _availableDates

    private val _availableSlotsForDate = MutableStateFlow<List<TimeSlot>>(emptyList())
    val availableSlotsForDate: StateFlow<List<TimeSlot>> = _availableSlotsForDate

    private val _bookingLoadComplete = MutableStateFlow(false)
    val bookingLoadComplete: StateFlow<Boolean> = _bookingLoadComplete

    fun setSlotDuration(minutes: Long) {
        _slotDurationMinutes = minutes
    }

    fun syncTenantId(tenantId: Long) {
        if (tenantId > 0 && prefs.getLong("tenant_id", 0L) != tenantId) {
            prefs.edit().putLong("tenant_id", tenantId).apply()
        }
    }

    fun loadBookingData(dentistStaffId: Long, tenantId: Long = currentTenantId) {
        if (tenantId == -1L || tenantId == 0L) return

        viewModelScope.launch {
            _isLoading.value = true
            _bookingLoadComplete.value = false
            _errorMessage.value = null
            try {
                val now = OffsetDateTime.now(ZoneOffset.UTC)
                val fromStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"
                val toStr = now.plusMonths(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"

                val appointmentsDef = async { apiService.getAppointments(tenantId, from = fromStr, to = toStr) }
                val blockersDef = async { apiService.getBlockers(tenantId) }
                val workingHoursDef = async { apiService.getWorkingHours(tenantId, dentistStaffId) }
                val tenantDef = async { apiService.getTenantDetails(tenantId) }
                val roomsDef = async { apiService.getRooms(tenantId) }

                val (appointmentsRes, blockersRes, workingHoursRes, tenantRes, roomsRes) =
                    awaitAll(appointmentsDef, blockersDef, workingHoursDef, tenantDef, roomsDef)
                        .map { it as retrofit2.Response<*> }

                @Suppress("UNCHECKED_CAST")
                if (appointmentsRes.isSuccessful) {
                    _bookingAppointments.value = (appointmentsRes.body() as? List<AppointmentResponse> ?: emptyList())
                        .filter { it.dentistStaffId == dentistStaffId && it.status != "CANCELLED" }
                } else {
                    Log.w(TAG, "getAppointments failed: ${appointmentsRes.code()} - cannot show available slots without appointment data")
                    _errorMessage.value = "Nie mozna pobrac danych o wizytach. Sprobuj ponownie."
                    _isLoading.value = false
                    _bookingLoadComplete.value = true
                    return@launch
                }
                @Suppress("UNCHECKED_CAST")
                if (blockersRes.isSuccessful) {
                    _bookingBlockers.value = (blockersRes.body() as? List<ScheduleBlockerDTO> ?: emptyList())
                        .filter { it.staffId == 0L || it.staffId == dentistStaffId }
                }
                @Suppress("UNCHECKED_CAST")
                if (workingHoursRes.isSuccessful) {
                    _bookingWorkingHours = workingHoursRes.body() as? List<StaffWorkingHoursDTO> ?: emptyList()
                } else {
                    try {
                        val staffRes = apiService.getStaffMembers(tenantId)
                        if (staffRes.isSuccessful) {
                            val staff = staffRes.body() ?: emptyList()
                            val dentist = staff.find { it.id == dentistStaffId }
                            if (dentist != null) {
                                val start = dentist.workingHoursStart ?: "08:00"
                                val end = dentist.workingHoursEnd ?: "16:00"
                                _bookingWorkingHours = (1..5).map { day ->
                                    StaffWorkingHoursDTO(
                                        id = 0L,
                                        staffMemberId = dentistStaffId,
                                        dayOfWeek = day,
                                        startTime = start,
                                        endTime = end
                                    )
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
                if (tenantRes.isSuccessful) {
                    @Suppress("UNCHECKED_CAST")
                    val firstLoc = (tenantRes.body() as? TenantResponse)?.locations?.firstOrNull()
                    _defaultLocationId = firstLoc?.id ?: 0L
                }
                @Suppress("UNCHECKED_CAST")
                if (roomsRes.isSuccessful) {
                    val allRooms = roomsRes.body() as? List<RoomResponse> ?: emptyList()
                    val matchingRooms = allRooms.filter { dentistStaffId in it.assignedStaffIds }
                    _defaultRoomId = matchingRooms.firstOrNull()?.id
                        ?: allRooms.firstOrNull()?.id
                }

                withContext(Dispatchers.Default) {
                    computeAvailableDates()
                }
                _bookingLoadComplete.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error loading booking data: ${e.message}")
                _errorMessage.value = "Nie udalo sie zaladowac danych. Sprawdz polaczenie z serwerem."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun computeAvailableDates() {
        val appointments = _bookingAppointments.value
        val blockers = _bookingBlockers.value
        val workingHours = _bookingWorkingHours

        val today = LocalDate.now()
        val endRange = today.plusMonths(3)
        val dates = mutableSetOf<LocalDate>()

        if (workingHours.isNotEmpty()) {
            var cursor = today.plusDays(1)
            while (!cursor.isAfter(endRange)) {
                val dow = cursor.dayOfWeek.value
                val wh = workingHours.find { it.dayOfWeek == dow }
                if (wh != null) {
                    val whStart = parseTime(wh.startTime) ?: continue
                    val whEnd = parseTime(wh.endTime) ?: continue
                    if (hasFreeWindow(cursor, whStart, whEnd, appointments, blockers, _slotDurationMinutes)) {
                        dates.add(cursor)
                    }
                }
                cursor = cursor.plusDays(1)
            }
        }

        _availableDates.value = dates
    }

    private fun parseTime(time: String): LocalTime? {
        return try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            try {
                LocalTime.parse(time)
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun hasFreeWindow(
        date: LocalDate,
        whStart: LocalTime,
        whEnd: LocalTime,
        appointments: List<AppointmentResponse>,
        blockers: List<ScheduleBlockerDTO>,
        slotDurationMinutes: Long = 30
    ): Boolean {
        val dayStart = date.atTime(whStart).atOffset(ZoneOffset.UTC)
        val dayEnd = date.atTime(whEnd).atOffset(ZoneOffset.UTC)

        val occupied = mutableListOf<Pair<OffsetDateTime, OffsetDateTime>>()

        for (appt in appointments) {
            val apptStart = parseDateTime(appt.startAt) ?: continue
            val apptEnd = parseDateTime(appt.endAt) ?: continue
            if (apptStart.isBefore(dayEnd) && apptEnd.isAfter(dayStart)) {
                occupied.add(Pair(maxOf(apptStart, dayStart), minOf(apptEnd, dayEnd)))
            }
        }

        for (b in blockers) {
            val bStart = parseDateTime(b.startAt) ?: continue
            val bEnd = parseDateTime(b.endAt) ?: continue
            if (bStart.isBefore(dayEnd) && bEnd.isAfter(dayStart)) {
                occupied.add(Pair(maxOf(bStart, dayStart), minOf(bEnd, dayEnd)))
            }
        }

        val sorted = occupied.sortedBy { it.first }
        var cursor = dayStart
        for ((occStart, occEnd) in sorted) {
            if (cursor.isBefore(occStart)) {
                if (cursor.plusMinutes(slotDurationMinutes).isBefore(occStart) || cursor.plusMinutes(slotDurationMinutes).isEqual(occStart)) {
                    return true
                }
            }
            if (occEnd.isAfter(cursor)) cursor = occEnd
        }
        if (cursor.isBefore(dayEnd)) {
            if (cursor.plusMinutes(slotDurationMinutes).isBefore(dayEnd) || cursor.plusMinutes(slotDurationMinutes).isEqual(dayEnd)) {
                return true
            }
        }
        return false
    }

    fun computeAvailableSlotsForDate(date: LocalDate, slotDurationMinutes: Long = 30) {
        viewModelScope.launch {
            val slots = withContext(Dispatchers.Default) {
                computeSlotsSync(date, slotDurationMinutes)
            }
            _availableSlotsForDate.value = slots
        }
    }

    private fun computeSlotsSync(date: LocalDate, slotDurationMinutes: Long = 30): List<TimeSlot> {
        val appointments = _bookingAppointments.value
        val blockers = _bookingBlockers.value
        val workingHours = _bookingWorkingHours

        val dow = date.dayOfWeek.value
        val wh = workingHours.find { it.dayOfWeek == dow }
        if (wh == null) {
            return emptyList()
        }

        val whStart = parseTime(wh.startTime) ?: return emptyList()
        val whEnd = parseTime(wh.endTime) ?: return emptyList()

        val dayStart = date.atTime(whStart).atOffset(ZoneOffset.UTC)
        val dayEnd = date.atTime(whEnd).atOffset(ZoneOffset.UTC)

        val availableWindows = mutableListOf<TimeSlot>()

        val occupied = mutableListOf<Pair<OffsetDateTime, OffsetDateTime>>()

        for (appt in appointments) {
            val apptStart = parseDateTime(appt.startAt) ?: continue
            val apptEnd = parseDateTime(appt.endAt) ?: continue
            if (apptStart.isBefore(dayEnd) && apptEnd.isAfter(dayStart)) {
                occupied.add(Pair(maxOf(apptStart, dayStart), minOf(apptEnd, dayEnd)))
            }
        }

        for (b in blockers) {
            val bStart = parseDateTime(b.startAt) ?: continue
            val bEnd = parseDateTime(b.endAt) ?: continue
            if (bStart.isBefore(dayEnd) && bEnd.isAfter(dayStart)) {
                occupied.add(Pair(maxOf(bStart, dayStart), minOf(bEnd, dayEnd)))
            }
        }

        val sorted = occupied.sortedBy { it.first }

        var cursor = dayStart
        for ((occStart, occEnd) in sorted) {
            if (cursor.isBefore(occStart)) {
                addSubSlots(availableWindows, cursor, occStart, _defaultLocationId, _defaultRoomId, slotDurationMinutes)
            }
            if (occEnd.isAfter(cursor)) {
                cursor = occEnd
            }
        }
        if (cursor.isBefore(dayEnd)) {
            addSubSlots(availableWindows, cursor, dayEnd, _defaultLocationId, _defaultRoomId, slotDurationMinutes)
        }

        return availableWindows.sortedBy { it.time }
    }

    private fun addSubSlots(
        list: MutableList<TimeSlot>,
        from: OffsetDateTime,
        to: OffsetDateTime,
        locationId: Long,
        roomId: Long?,
        slotDurationMinutes: Long = 30
    ) {
        var start = from
        while (start.plusMinutes(slotDurationMinutes).isBefore(to) || start.plusMinutes(slotDurationMinutes).isEqual(to)) {
            val end = start.plusMinutes(slotDurationMinutes)
            list.add(
                TimeSlot(
                    time = start.toLocalTime(),
                    endTime = end.toLocalTime(),
                    startIso = formatIso(start),
                    endIso = formatIso(end),
                    locationId = locationId,
                    roomId = roomId
                )
            )
            start = end
        }
    }

    private fun parseDateTime(iso: String): OffsetDateTime? {
        return try {
            OffsetDateTime.parse(iso)
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atOffset(ZoneOffset.UTC)
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun formatIso(dt: OffsetDateTime): String {
        return dt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    fun resetBookingState() {
        _bookingAppointments.value = emptyList()
        _bookingBlockers.value = emptyList()
        _bookingWorkingHours = emptyList()
        _defaultLocationId = 0L
        _defaultRoomId = null
        _slotDurationMinutes = 30
        _availableDates.value = emptySet()
        _availableSlotsForDate.value = emptyList()
        _bookingLoadComplete.value = false
    }

    // ── Existing CRUD operations ─────────────────────────────────────────────

    fun fetchAppointments(date: LocalDate) {
        val tenantId = currentTenantId
        val userId = currentUserId
        if (tenantId == -1L || userId == -1L) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val startOfDay = "${date}T00:00:00Z"
                val endOfDay = "${date}T23:59:59Z"
                val response = apiService.getAppointments(tenantId, from = startOfDay, to = endOfDay)

                if (response.isSuccessful) {
                    val allVisits = response.body() ?: emptyList()
                    _appointments.value = if (userRole == "PATIENT") {
                        allVisits.filter { it.patientId == userId }
                    } else {
                        allVisits
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetch: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAppointmentDetails(appointmentId: Long) {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

        viewModelScope.launch {
            try {
                val response = apiService.getAppointmentDetails(tenantId, appointmentId)
                if (response.isSuccessful) {
                    _selectedAppointment.value = response.body()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error details: ${e.message}")
            }
        }
    }

    fun createAppointment(
        locId: Long, room: Long?, docId: Long, patId: Long?,
        servId: Long?, start: String, end: String, note: String,
        onSuccess: () -> Unit, tenantId: Long = currentTenantId
    ) {
        if (tenantId == -1L || tenantId == 0L || _isCreating.value) return

        viewModelScope.launch {
            _isCreating.value = true
            _isLoading.value = true
            try {
                var effectivePatientId = patId
                if (effectivePatientId == null && currentUserId > 0) {
                    val ensureRes = apiService.ensurePatient(
                        tenantId = tenantId,
                        userId = currentUserId,
                        firstName = prefs.getString("user_first_name", "") ?: "",
                        lastName = prefs.getString("user_last_name", "") ?: "",
                        email = prefs.getString("user_email", "") ?: "",
                        phone = prefs.getString("user_phone", "") ?: ""
                    )
                    if (ensureRes.isSuccessful) {
                        effectivePatientId = ensureRes.body()?.id
                    }
                }

                val request = CreateAppointmentRequest(
                    locationId = locId, roomId = room, dentistStaffId = docId,
                    patientId = effectivePatientId, serviceItemId = servId, startAt = start,
                    endAt = end, createdByUserId = currentUserId, notes = note
                )
                val response = apiService.createAppointment(tenantId, request)
                if (response.isSuccessful) onSuccess()
                else {
                    val errorMsg = when (response.code()) {
                        409 -> "Termin jest juz zajety. Wybierz inny."
                        400 -> "Bledne dane rezerwacji."
                        else -> "Blad serwera (${response.code()}). Sprobuj ponownie."
                    }
                    Log.e(TAG, "Error create appointment: ${response.code()} ${response.errorBody()?.string()}")
                    _errorMessage.value = errorMsg
                    _isCreating.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error create: ${e.message}")
                _errorMessage.value = "Brak polaczenia z serwerem. Sprawdz internet."
                _isCreating.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAppointment(
        appointmentId: Long, start: String, end: String,
        servId: Long?, roomId: Long?, note: String,
        onSuccess: () -> Unit
    ) {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

        viewModelScope.launch {
            try {
                val request = UpdateAppointmentRequest(
                    startAt = start, endAt = end, serviceItemId = servId,
                    roomId = roomId, notes = note
                )
                val response = apiService.updateAppointment(tenantId, appointmentId, request)
                if (response.isSuccessful) onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error update: ${e.message}")
            }
        }
    }

    fun completeAppointment(appointmentId: Long, onSuccess: () -> Unit) {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

        viewModelScope.launch {
            try {
                val response = apiService.completeAppointment(tenantId, appointmentId)
                if (response.isSuccessful) onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error complete: ${e.message}")
            }
        }
    }

    fun cancelAppointment(appointmentId: Long, onSuccess: () -> Unit) {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

        viewModelScope.launch {
            try {
                val response = apiService.cancelAppointment(tenantId, appointmentId)
                if (response.isSuccessful) onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error cancel: ${e.message}")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

data class TimeSlot(
    val time: LocalTime,
    val endTime: LocalTime,
    val startIso: String,
    val endIso: String,
    val locationId: Long,
    val roomId: Long? = null
)
