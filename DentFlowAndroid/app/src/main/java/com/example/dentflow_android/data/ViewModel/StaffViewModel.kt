package com.example.dentflow_android.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authService: AuthService,
    private val prefs: SharedPreferences // Dodajemy prefs dla dynamicznego tenantId
) : ViewModel() {

    private val _staffMembers = MutableStateFlow<List<StaffMemberResponse>>(emptyList())
    val staffMembers: StateFlow<List<StaffMemberResponse>> = _staffMembers

    private val _services = MutableStateFlow<List<ServiceCatalogItemDTO>>(emptyList())
    val services: StateFlow<List<ServiceCatalogItemDTO>> = _services

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val TAG = "STAFF_VM_DEBUG"

    // Dynamiczne pobieranie tenantId z sesji
    private val currentTenantId: Long
        get() = prefs.getLong("tenant_id", -1L)

    private fun hasValidSession(): Boolean {
        if (currentTenantId == -1L) {
            Log.e(TAG, "BŁĄD: Próba operacji na personelu bez tenantId!")
            return false
        }
        return true
    }

    // --- ŁADOWANIE DANYCH ---

    fun loadAllData() {
        if (!hasValidSession()) return

        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Rozpoczynam pobieranie personelu i usług dla tenantId: $currentTenantId")
            try {
                // Wykonujemy zapytania równolegle dla szybkości
                val staffDef = async { apiService.getStaffMembers(currentTenantId) }
                val servicesDef = async { apiService.getServices(currentTenantId) }

                val sRes = staffDef.await()
                val vRes = servicesDef.await()

                if (sRes.isSuccessful) {
                    _staffMembers.value = sRes.body() ?: emptyList()
                    Log.d(TAG, "Pobrano pracowników: ${sRes.body()?.size}")
                } else {
                    Log.e(TAG, "Błąd pobierania pracowników: ${sRes.code()}")
                }

                if (vRes.isSuccessful) {
                    _services.value = vRes.body() ?: emptyList()
                    Log.d(TAG, "Pobrano usługi z katalogu: ${vRes.body()?.size}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek podczas loadAllData: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadStaff() {
        if (!hasValidSession()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getStaffMembers(currentTenantId)
                if (response.isSuccessful) {
                    _staffMembers.value = response.body() ?: emptyList()
                    Log.d(TAG, "Odświeżono listę personelu")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loadStaff: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }


    // --- ZARZĄDZANIE PERSONELEM ---

    fun addStaff(fName: String, lName: String, profession: String, email: String, password: String, phone: String) {
        if (!hasValidSession()) return

        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "ETAP 1: Rejestracja konta użytkownika dla: $email")
            try {
                val registerRequest = RegisterRequest(
                    email = email,
                    password = password,
                    firstName = fName,
                    lastName = lName,
                    phone = phone
                )

                // 1. Najpierw tworzymy konto w systemie Auth
                val authResponse = authService.register(registerRequest)

                if (authResponse.isSuccessful && authResponse.body() != null) {
                    val createdUserId = authResponse.body()!!.userId
                    Log.d(TAG, "Konto utworzone pomyślnie. UserId: $createdUserId. ETAP 2: Przypisanie do kliniki")

                    if (createdUserId != 0L) {
                        val staffRequest = CreateStaffMemberRequest(
                            userId = createdUserId,
                            displayName = "$fName $lName",
                            profession = profession
                        )

                        // 2. Potem tworzymy rekord pracownika w konkretnej klinice (tenantId)
                        val coreResponse = apiService.createStaffMember(currentTenantId, staffRequest)
                        if (coreResponse.isSuccessful) {
                            Log.d(TAG, "Pracownik pomyślnie przypisany do kliniki $currentTenantId")
                            loadStaff()
                        } else {
                            Log.e(TAG, "Błąd ETAPU 2 (przypisanie): ${coreResponse.code()}")
                        }
                    }
                } else {
                    Log.e(TAG, "Błąd ETAPU 1 (rejestracja): ${authResponse.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek addStaff: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStaff(staffId: Long, fName: String, lName: String, profession: String, userId: Long) {
        if (!hasValidSession()) return

        viewModelScope.launch {
            Log.d(TAG, "Aktualizacja pracownika ID: $staffId")
            try {
                val updateRequest = UpdateStaffMemberRequest(
                    userId = userId,
                    displayName = "$fName $lName",
                    profession = profession
                )
                val response = apiService.updateStaffMember(currentTenantId, staffId, updateRequest)
                if (response.isSuccessful) {
                    Log.d(TAG, "Pracownik zaktualizowany")
                    loadStaff()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception updateStaff: ${e.message}")
            }
        }
    }

    fun deleteStaff(staffId: Long) {
        if (!hasValidSession()) return

        viewModelScope.launch {
            Log.d(TAG, "Usuwanie pracownika ID: $staffId z kliniki $currentTenantId")
            try {
                val response = apiService.deleteStaffMember(currentTenantId, staffId)
                if (response.isSuccessful) {
                    Log.d(TAG, "Pracownik usunięty")
                    loadStaff()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception deleteStaff: ${e.message}")
            }
        }
    }
}