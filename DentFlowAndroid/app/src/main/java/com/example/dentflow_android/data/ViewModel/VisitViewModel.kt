package com.example.dentflow_android.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VisitWithPatient(
    val visit: AppointmentResponse,
    val patient: PatientResponse?
)

@HiltViewModel
class VisitViewModel @Inject constructor(
    private val apiService: ApiService,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _visits = MutableStateFlow<List<VisitWithPatient>>(emptyList())
    val visits: StateFlow<List<VisitWithPatient>> = _visits

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val TAG = "VISIT_VM_DEBUG"

    private val currentTenantId: Long
        get() = prefs.getLong("tenant_id", -1L)

    private val userRole: String
        get() = prefs.getString("user_role", "USER") ?: "USER"

    private val isPatient: Boolean
        get() = userRole != "OWNER" && userRole != "DOCTOR"

    init {
        refreshVisits()
    }

    fun refreshVisits() {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

        if (isPatient) {
            fetchMyVisits(tenantId)
        } else {
            fetchVisitsWithPatients(tenantId)
        }
    }

    /** Dla pacjenta — tylko jego własne wizyty przez endpoint /my */
    private fun fetchMyVisits(tenantId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getMyAppointments(tenantId)
                when {
                    response.isSuccessful -> {
                        _visits.value = (response.body() ?: emptyList()).map { appointment ->
                            VisitWithPatient(visit = appointment, patient = null)
                        }
                    }
                    response.code() == 403 -> {
                        _errorMessage.value = "Brak uprawnień do pobrania wizyt."
                        Log.e(TAG, "403 Forbidden on /appointments/my")
                    }
                    else -> {
                        _errorMessage.value = "Błąd serwera: ${response.code()}"
                        Log.e(TAG, "Error ${response.code()} on /appointments/my")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Brak połączenia z serwerem."
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
                                val patientRes = apiService.getPatientById(tenantId, appointment.patientId)
                                VisitWithPatient(
                                    visit = appointment,
                                    patient = if (patientRes.isSuccessful) patientRes.body() else null
                                )
                            }
                        }.awaitAll()
                        _visits.value = combinedList
                    }
                    response.code() == 403 -> {
                        _errorMessage.value = "Brak uprawnień do listy wizyt."
                        Log.e(TAG, "403 Forbidden on /appointments")
                    }
                    else -> {
                        _errorMessage.value = "Błąd serwera: ${response.code()}"
                        Log.e(TAG, "Error ${response.code()} on /appointments")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Brak połączenia z serwerem."
                Log.e(TAG, "Exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchPatientHistory(patientId: Long) {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

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
}