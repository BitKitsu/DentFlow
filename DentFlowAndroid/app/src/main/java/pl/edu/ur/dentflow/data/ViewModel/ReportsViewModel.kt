package pl.edu.ur.dentflow.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.edu.ur.dentflow.data.remote.ApiService
import pl.edu.ur.dentflow.data.remote.PatientResponse
import pl.edu.ur.dentflow.data.remote.RoomResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _patients = MutableStateFlow<List<PatientResponse>>(emptyList())
    val patients: StateFlow<List<PatientResponse>> = _patients.asStateFlow()

    private val _rooms = MutableStateFlow<List<RoomResponse>>(emptyList())
    val rooms: StateFlow<List<RoomResponse>> = _rooms.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val TAG = "REPORTS_VM_DEBUG"
    private val currentTenantId: Long get() = prefs.getLong("tenant_id", -1L)

    fun loadData() {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val patientsRes = apiService.getPatients(tenantId)
                if (patientsRes.isSuccessful) {
                    _patients.value = patientsRes.body() ?: emptyList()
                }

                val roomsRes = apiService.getRooms(tenantId)
                if (roomsRes.isSuccessful) {
                    _rooms.value = roomsRes.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd ładowania danych: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
