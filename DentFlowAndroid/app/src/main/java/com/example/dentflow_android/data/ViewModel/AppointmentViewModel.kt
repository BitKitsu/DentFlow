package com.example.dentflow_android.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
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

    private val TAG = "AppointmentViewModel"

    private val currentTenantId: Long
        get() = prefs.getLong("tenant_id", -1L)

    private val currentUserId: Long
        get() = prefs.getLong("user_id", -1L)

    private val userRole: String
        get() = prefs.getString("user_role", "PATIENT") ?: "PATIENT"

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
        locId: Long, room: Long, docId: Long, patId: Long,
        servId: Long, start: String, end: String, note: String,
        onSuccess: () -> Unit
    ) {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = CreateAppointmentRequest(
                    locationId = locId, roomId = room, dentistStaffId = docId,
                    patientId = patId, serviceItemId = servId, startAt = start,
                    endAt = end, createdByUserId = currentUserId, notes = note
                )
                val response = apiService.createAppointment(tenantId, request)
                if (response.isSuccessful) onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error create: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAppointment(
        appointmentId: Long, start: String, end: String,
        servId: Long, roomId: Long, note: String,
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
}