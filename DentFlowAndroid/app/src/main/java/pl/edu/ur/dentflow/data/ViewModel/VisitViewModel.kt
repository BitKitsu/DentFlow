package pl.edu.ur.dentflow.data.ViewModel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.edu.ur.dentflow.data.remote.*
import pl.edu.ur.dentflow.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject

data class VisitWithPatient(
    val visit: AppointmentResponse,
    val patient: PatientResponse?
)

@HiltViewModel
class VisitViewModel @Inject constructor(
    private val apiService: ApiService,
    private val prefs: SharedPreferences,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) : ViewModel() {

    private val _visits = MutableStateFlow<List<VisitWithPatient>>(emptyList())
    val visits: StateFlow<List<VisitWithPatient>> = _visits

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val TAG = "VISIT_VM_DEBUG"

    private val currentTenantId: Long
        get() {
            val id = prefs.getLong("tenant_id", -1L)
            return if (id <= 0L) -1L else id
        }

    private val userRole: String
        get() = prefs.getString("user_role", "PATIENT") ?: "PATIENT"

    val isPatient: Boolean
        get() = userRole == "PATIENT"

    val isReadOnly: Boolean
        get() = userRole == "ASSISTANT"

    val currentUserId: Long
        get() = prefs.getLong("user_id", -1L)

    init {
        refreshVisits()
        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "tenant_id") {
                val newTenantId = prefs.getLong("tenant_id", -1L)
                if (newTenantId > 0L) {
                    refreshVisits()
                }
            }
        }
    }

    fun refreshVisits() {
        val tenantId = currentTenantId
        if (tenantId <= 0L) return

        if (isPatient) {
            fetchMyVisits(tenantId)
        } else {
            fetchVisitsWithPatients(tenantId)
        }
    }

    /** Dla pacjenta - tylko jego własne wizyty przez endpoint /my */
    private fun fetchMyVisits(tenantId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getMyAppointments(tenantId)
                when {
                    response.isSuccessful -> {
                        val myPatient = PatientResponse(
                            id = 0L,
                            tenantId = tenantId,
                            userId = currentUserId,
                            firstName = prefs.getString("user_first_name", "") ?: "",
                            lastName = prefs.getString("user_last_name", "") ?: "",
                            email = prefs.getString("user_email", "") ?: "",
                            phone = prefs.getString("user_phone", "") ?: ""
                        )
                        _visits.value = (response.body() ?: emptyList()).map { appointment ->
                            VisitWithPatient(visit = appointment, patient = myPatient)
                        }
                    }
                    response.code() == 403 -> {
                        _errorMessage.value = context.getString(R.string.error_no_permission_visits)
                        Log.e(TAG, "403 Forbidden on /appointments/my")
                    }
                    else -> {
                        _errorMessage.value = extractErrorMessage(response.errorBody(), response.code())
                        Log.e(TAG, "Error ${response.code()} on /appointments/my")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.error_connection_server)
                Log.e(TAG, "Exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Dla admina/lekarza - pełna lista wizyt tenanta */
    private fun fetchVisitsWithPatients(tenantId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getAppointments(tenantId)
                when {
                    response.isSuccessful -> {
                        val appointmentList = response.body() ?: emptyList()
                        val combinedList = appointmentList.map { appointment ->
                            async {
                                try {
                                    val patientRes = appointment.patientId?.let { apiService.getPatientById(tenantId, it) }
                                    VisitWithPatient(
                                        visit = appointment,
                                        patient = if (patientRes != null && patientRes.isSuccessful) patientRes.body() else null
                                    )
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error fetching patient for appointment ${appointment.id}: ${e.message}")
                                    VisitWithPatient(visit = appointment, patient = null)
                                }
                            }
                        }.awaitAll()
                        _visits.value = combinedList
                    }
                    response.code() == 403 -> {
                        _errorMessage.value = context.getString(R.string.error_no_permission_visit_list)
                        Log.e(TAG, "403 Forbidden on /appointments")
                    }
                    else -> {
                        _errorMessage.value = extractErrorMessage(response.errorBody(), response.code())
                        Log.e(TAG, "Error ${response.code()} on /appointments")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.error_connection_server)
                Log.e(TAG, "Exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun confirmAppointment(appointmentId: Long) {
        val tenantId = currentTenantId
        if (tenantId <= 0L) return

        viewModelScope.launch {
            try {
                if (apiService.confirmAppointment(tenantId, appointmentId).isSuccessful) {
                    refreshVisits()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception confirmAppointment: ${e.message}")
            }
        }
    }

    fun cancelAppointment(appointmentId: Long) {
        val tenantId = currentTenantId
        if (tenantId <= 0L) return

        viewModelScope.launch {
            try {
                if (apiService.cancelAppointment(tenantId, appointmentId).isSuccessful) {
                    refreshVisits()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception cancelAppointment: ${e.message}")
            }
        }
    }

    fun completeAppointment(appointmentId: Long) {
        val tenantId = currentTenantId
        if (tenantId <= 0L) return

        viewModelScope.launch {
            try {
                if (apiService.completeAppointment(tenantId, appointmentId).isSuccessful) {
                    refreshVisits()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception completeAppointment: ${e.message}")
            }
        }
    }

    fun fetchPatientHistory(patientId: Long) {
        val tenantId = currentTenantId
        if (tenantId <= 0L) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getPatientVisits(tenantId, patientId)
                if (response.isSuccessful) {
                    val historyList = response.body() ?: emptyList()

                    val patientRes = apiService.getPatientById(tenantId, patientId)
                    val patientData = if (patientRes.isSuccessful) patientRes.body() else null

                    _visits.value = historyList.map {
                        VisitWithPatient(visit = it, patient = patientData)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadReport(from: String, to: String, context: android.content.Context) {
        val tenantId = currentTenantId
        if (tenantId <= 0L) return

        viewModelScope.launch {
            try {
                val response = apiService.getAppointmentReportPdf(tenantId, from, to)
                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()
                    if (bytes != null && bytes.size > 100) {
                        savePdfToDisk(bytes, context, "Raport_Wizyt_${from}_$to.pdf")
                    } else {
                        android.widget.Toast.makeText(context, context.getString(R.string.error_no_data_report), android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val msg = extractErrorMessage(response.errorBody(), response.code())
                    Log.e(TAG, "Błąd pobierania PDF: $msg")
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd sieci: ${e.message}")
                android.widget.Toast.makeText(context, context.getString(R.string.error_connection_server), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun downloadRoomOccupancyReport(from: String, to: String, roomId: Long?, context: android.content.Context) {
        val tenantId = currentTenantId
        if (tenantId <= 0L) return

        viewModelScope.launch {
            try {
                val response = if (roomId != null && roomId > 0) {
                    apiService.getSingleRoomOccupancyReport(tenantId, roomId, from, to)
                } else {
                    apiService.getRoomOccupancyReport(tenantId, from, to)
                }
                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()
                    if (bytes != null && bytes.size > 100) {
                        val suffix = if (roomId != null && roomId > 0) "_gabinet_$roomId" else ""
                        savePdfToDisk(bytes, context, "Raport_Oblzenia${suffix}_${from}_$to.pdf")
                    } else {
                        android.widget.Toast.makeText(context, context.getString(R.string.error_no_data_report), android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val msg = extractErrorMessage(response.errorBody(), response.code())
                    Log.e(TAG, "Błąd pobierania PDF obłożenia: $msg")
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd sieci obłożenie: ${e.message}")
                android.widget.Toast.makeText(context, context.getString(R.string.error_connection_server), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun downloadMyVisitsReport(from: String, to: String, context: android.content.Context) {
        val tenantId = currentTenantId
        if (tenantId <= 0L) return

        viewModelScope.launch {
            try {
                val response = apiService.getMyPatientHistoryReportPdf(tenantId, from = from, to = to)
                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()
                    if (bytes != null && bytes.size > 100) {
                        savePdfToDisk(bytes, context, "Moje_Wizyty_${from}_$to.pdf")
                    } else {
                        android.widget.Toast.makeText(context, context.getString(R.string.error_no_data_report), android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val msg = extractErrorMessage(response.errorBody(), response.code())
                    Log.e(TAG, "Błąd pobierania PDF moich wizyt: $msg")
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd sieci: ${e.message}")
                android.widget.Toast.makeText(context, context.getString(R.string.error_connection_server), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun downloadPatientHistoryReport(patientId: Long, from: String? = null, to: String? = null, context: android.content.Context) {
        val tenantId = currentTenantId
        if (tenantId <= 0L) return

        viewModelScope.launch {
            try {
                val response = apiService.getPatientHistoryReportPdf(tenantId, patientId, from = from, to = to)
                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()
                    if (bytes != null && bytes.size > 100) {
                        savePdfToDisk(bytes, context, "Historia_Pacjenta_$patientId.pdf")
                    } else {
                        android.widget.Toast.makeText(context, context.getString(R.string.error_no_data_report), android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val msg = extractErrorMessage(response.errorBody(), response.code())
                    Log.e(TAG, "Błąd pobierania PDF historii: $msg")
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd sieci historia: ${e.message}")
                android.widget.Toast.makeText(context, context.getString(R.string.error_connection_server), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun extractErrorMessage(body: ResponseBody?, fallbackCode: Int): String {
        return try {
            body?.string()?.takeIf { it.isNotBlank() } ?: context.getString(R.string.error_booking_server, fallbackCode)
        } catch (_: Exception) {
            context.getString(R.string.error_booking_server, fallbackCode)
        }
    }

    private fun savePdfToDisk(bytes: ByteArray, context: android.content.Context, filename: String) {
        try {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(bytes)
                }
                android.widget.Toast.makeText(context, context.getString(R.string.success_pdf_saved), android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(context, context.getString(R.string.error_pdf_save), android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Błąd zapisu pliku: ${e.message}")
            android.widget.Toast.makeText(context, context.getString(R.string.error_file_connection, e.message ?: ""), android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}