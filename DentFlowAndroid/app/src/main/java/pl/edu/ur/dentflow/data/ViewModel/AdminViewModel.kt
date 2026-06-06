package pl.edu.ur.dentflow.data.ViewModel

import android.util.Log
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.edu.ur.dentflow.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AdminViewModel"

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val apiService: ApiService,
    private val prefs: SharedPreferences
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
                Log.e(TAG, "loadStats error", e)
            }
        }
    }
}