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

    private val TAG = "VISIT_VM_DEBUG"

    private val currentTenantId: Long
        get() = prefs.getLong("tenant_id", -1L)

    init {
        refreshVisits()
    }

    fun refreshVisits() {
        val tenantId = currentTenantId
        if (tenantId == -1L) return
        fetchVisitsWithPatients(tenantId)
    }

    private fun fetchVisitsWithPatients(tenantId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getAppointments(tenantId)
                if (response.isSuccessful) {
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
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
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