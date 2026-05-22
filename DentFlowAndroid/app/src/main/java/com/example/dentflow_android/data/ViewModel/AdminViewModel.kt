package com.example.dentflow_android.data.ViewModel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val apiService: ApiService,
    private val prefs: SharedPreferences // Wstrzykujemy SharedPreferences
) : ViewModel() {

    private val _visitCount = MutableStateFlow("0")
    val visitCount: StateFlow<String> = _visitCount

    private val _patientCount = MutableStateFlow("0")
    val patientCount: StateFlow<String> = _patientCount

    // Pobieramy tenantId z SharedPreferences zamiast przekazywać go w parametrze
    private val currentTenantId: Long
        get() = prefs.getLong("tenant_id", -1L)

    fun loadStats() {
        // Jeśli nie mamy zapisanego tenantId, przerywamy ładowanie
        if (currentTenantId == -1L) return

        viewModelScope.launch {
            try {
                // Używamy dynamicznego ID kliniki
                val visitsRes = apiService.getAppointments(currentTenantId)
                if (visitsRes.isSuccessful) {
                    _visitCount.value = (visitsRes.body()?.size ?: 0).toString()
                }

                val patientsRes = apiService.getPatients(currentTenantId)
                if (patientsRes.isSuccessful) {
                    _patientCount.value = (patientsRes.body()?.size ?: 0).toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}