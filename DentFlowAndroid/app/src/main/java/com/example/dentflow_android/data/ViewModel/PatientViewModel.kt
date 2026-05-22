package com.example.dentflow_android.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.ApiService
import com.example.dentflow_android.data.remote.CreatePatientRequest
import com.example.dentflow_android.data.remote.PatientResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val apiService: ApiService,
    private val prefs: SharedPreferences // Wstrzykujemy prefs, aby brać dynamiczne tenantId
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
            Log.d(TAG, "Pobieranie pacjentów dla tenantId: $currentTenantId")
            try {
                val response = apiService.getPatients(currentTenantId)
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    _patients.value = list
                    Log.d(TAG, "Pobrano pomyślnie ${list.size} pacjentów")

                    list.forEach {
                        Log.d(TAG, "Pacjent w bazie -> ID: ${it.id}, Nazwisko: ${it.lastName}, Telefon: ${it.phone}")
                    }
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

    // 2. DODAWANIE PACJENTA
    fun addPatient(firstName: String, lastName: String, email: String, phone: String) {
        if (!checkTenantId()) return

        viewModelScope.launch {
            Log.d(TAG, "Próba dodania pacjenta: $firstName $lastName, tel: $phone")
            try {
                val request = CreatePatientRequest(
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone,
                    email = email,
                    notes = ""
                )

                val response = apiService.createPatient(currentTenantId, request)
                if (response.isSuccessful) {
                    Log.d(TAG, "Dodano pacjenta pomyślnie. Nowe ID: ${response.body()?.id}")
                    fetchPatients() // Odświeżamy listę
                } else {
                    Log.e(TAG, "Serwer odrzucił żądanie dodania: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd krytyczny podczas dodawania: ${e.message}")
            }
        }
    }

    // 3. EDYCJA PACJENTA
    fun updatePatient(id: Long, firstName: String, lastName: String, email: String, phone: String) {
        if (!checkTenantId()) return

        viewModelScope.launch {
            Log.d(TAG, "Edycja pacjenta ID: $id -> Nowe dane: $lastName, $phone")
            try {
                val request = CreatePatientRequest(
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone,
                    email = email,
                    notes = ""
                )

                val response = apiService.updatePatient(currentTenantId, id, request)
                if (response.isSuccessful) {
                    Log.d(TAG, "Zaktualizowano pacjenta pomyślnie (ID: $id)")
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
            Log.d(TAG, "Próba usunięcia pacjenta ID: $id")
            try {
                val response = apiService.deletePatient(currentTenantId, id)
                if (response.isSuccessful) {
                    Log.d(TAG, "Pacjent ID: $id usunięty z bazy")
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