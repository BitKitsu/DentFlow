package pl.edu.ur.dentflow.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.edu.ur.dentflow.data.remote.ApiService
import pl.edu.ur.dentflow.data.remote.AuthResponse
import pl.edu.ur.dentflow.data.remote.AuthService
import pl.edu.ur.dentflow.data.remote.CreatePatientRequest
import pl.edu.ur.dentflow.data.remote.UpdatePatientRequest
import pl.edu.ur.dentflow.data.remote.PatientResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authService: AuthService,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _patients = MutableStateFlow<List<PatientResponse>>(emptyList())
    val patients: StateFlow<List<PatientResponse>> = _patients

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val TAG = "PATIENT_VM_DEBUG"

    // Dynamiczne pobieranie tenantId z sesji
    private val currentTenantId: Long
        get() = prefs.getLong("tenant_id", -1L)

    // Funkcja sprawdzająca czy mamy ID kliniki przed wykonaniem zapytania
    private fun checkTenantId(): Boolean {
        if (currentTenantId == -1L) {
            Log.e(TAG, "BŁĄD: Próba operacji na pacjentach bez przypisanego tenantId!")
            return false
        }
        return true
    }

    // 1. POBIERANIE PACJENTÓW
    fun fetchPatients() {
        if (!checkTenantId()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getPatients(currentTenantId)
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    _patients.value = list
                } else {
                    Log.e(TAG, "Błąd pobierania: Kod ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek sieciowy podczas pobierania: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Alias dla kompatybilności
    fun loadPatients() = fetchPatients()

    suspend fun checkUserByEmail(email: String): AuthResponse? {
        return try {
            val response = authService.getUserByEmail(email)
            if (response.isSuccessful && response.body() != null) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Błąd sprawdzania emaila: ${e.message}")
            null
        }
    }

    fun addPatient(
        firstName: String, lastName: String, email: String, phone: String, 
        dateOfBirth: String? = null, pesel: String? = null, gender: String? = null,
        addressStreet: String? = null, addressCity: String? = null, addressZip: String? = null, addressCountry: String? = null,
        userId: Long? = null, avatarUrl: String? = null
    ) {
        if (!checkTenantId()) return

        viewModelScope.launch {
            try {
                val request = CreatePatientRequest(
                    userId = userId,
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone,
                    email = email,
                    notes = "",
                    dateOfBirth = dateOfBirth,
                    pesel = pesel,
                    gender = gender,
                    addressStreet = addressStreet,
                    addressCity = addressCity,
                    addressZip = addressZip,
                    addressCountry = addressCountry,
                    avatarUrl = avatarUrl
                )

                val response = apiService.createPatient(currentTenantId, request)
                if (response.isSuccessful) {
                    fetchPatients() // Odświeżamy listę
                } else {
                    Log.e(TAG, "Serwer odrzucił żądanie dodania: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd krytyczny podczas dodawania: ${e.message}")
            }
        }
    }

    fun updatePatient(
        id: Long, firstName: String, lastName: String, email: String, phone: String, 
        dateOfBirth: String? = null, pesel: String? = null, gender: String? = null,
        addressStreet: String? = null, addressCity: String? = null, addressZip: String? = null, addressCountry: String? = null
    ) {
        if (!checkTenantId()) return

        viewModelScope.launch {
            try {
                val request = UpdatePatientRequest(
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone,
                    email = email,
                    notes = "",
                    dateOfBirth = dateOfBirth,
                    pesel = pesel,
                    gender = gender,
                    addressStreet = addressStreet,
                    addressCity = addressCity,
                    addressZip = addressZip,
                    addressCountry = addressCountry
                )

                val response = apiService.updatePatient(currentTenantId, id, request)
                if (response.isSuccessful) {
                    fetchPatients()
                } else {
                    Log.e(TAG, "Błąd edycji: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd krytyczny podczas edycji: ${e.message}")
            }
        }
    }

    // 4. USUWANIE PACJENTA
    fun deletePatient(id: Long) {
        if (!checkTenantId()) return

        viewModelScope.launch {
            try {
                val response = apiService.deletePatient(currentTenantId, id)
                if (response.isSuccessful) {
                    _patients.value = _patients.value.filter { it.id != id }
                } else {
                    Log.e(TAG, "Serwer nie pozwolił usunąć pacjenta: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd podczas usuwania: ${e.message}")
            }
        }
    }
}