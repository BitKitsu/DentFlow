package pl.edu.ur.dentflow.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.edu.ur.dentflow.data.remote.*
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

    private val _allStaff = MutableStateFlow<List<StaffMemberResponse>>(emptyList())
    val allStaff: StateFlow<List<StaffMemberResponse>> = _allStaff

    private val _services = MutableStateFlow<List<ServiceCatalogItemDTO>>(emptyList())
    val services: StateFlow<List<ServiceCatalogItemDTO>> = _services

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

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
            try {
                // Wykonujemy zapytania równolegle dla szybkości
                val staffDef = async { apiService.getStaffMembers(currentTenantId) }
                val servicesDef = async { apiService.getServices(currentTenantId) }

                val sRes = staffDef.await()
                val vRes = servicesDef.await()

                if (sRes.isSuccessful) {
                    _staffMembers.value = sRes.body() ?: emptyList()
                } else if (sRes.code() == 403) {
                    _errorMessage.value = "Brak uprawnień do przeglądania personelu."
                    Log.e(TAG, "403 Forbidden: getStaffMembers")
                } else {
                    Log.e(TAG, "Błąd pobierania pracowników: ${sRes.code()}")
                }

                if (vRes.isSuccessful) {
                    _services.value = vRes.body() ?: emptyList()
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
            _errorMessage.value = null
            try {
                val response = apiService.getStaffMembers(currentTenantId)
                when {
                    response.isSuccessful -> {
                        _staffMembers.value = response.body() ?: emptyList()
                    }
                    response.code() == 403 -> {
                        _errorMessage.value = "Brak uprawnień do zarządzania personelem."
                        Log.e(TAG, "403 Forbidden: loadStaff")
                    }
                    else -> Log.e(TAG, "Błąd loadStaff: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Brak połączenia z serwerem."
                Log.e(TAG, "Exception loadStaff: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllStaff() {
        viewModelScope.launch {
            try {
                val response = apiService.getAllStaffMembers()
                if (response.isSuccessful) {
                    _allStaff.value = response.body() ?: emptyList()
                } else {
                    Log.e(TAG, "Błąd pobierania wszystkich pracowników: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd pobierania wszystkich pracowników: ${e.message}")
            }
        }
    }


    // --- ZARZĄDZANIE PERSONELEM ---

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

    fun addStaff(fName: String, lName: String, profession: String, email: String, pass: String, phone: String, bio: String, userExists: Boolean, existingUserId: Long?, existingAvatarUrl: String?, workingHoursStart: String? = null, workingHoursEnd: String? = null) {
        if (!hasValidSession()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                var userId: Long? = existingUserId
                var userAvatarUrl: String? = existingAvatarUrl

                // 1. Jeśli użytkownik nie istnieje - utwórz nowe konto
                if (!userExists) {
                    val registerRequest = RegisterRequest(
                        email = email,
                        password = pass,
                        firstName = fName,
                        lastName = lName,
                        phone = phone
                    )

                    val authResponse = authService.register(registerRequest)
                    if (authResponse.isSuccessful && authResponse.body() != null) {
                        userId = authResponse.body()!!.userId
                        userAvatarUrl = authResponse.body()!!.avatarUrl
                    } else {
                        Log.e(TAG, "Błąd tworzenia konta: ${authResponse.code()}")
                        _isLoading.value = false
                        return@launch
                    }
                }

                // 2. Przypisz rolę DENTIST użytkownikowi
                if (userId != null && userId != 0L) {
                    val roleRequest = AssignRoleRequest(userId = userId, role = "DENTIST")
                    val roleResponse = authService.assignRole(roleRequest)
                    if (!roleResponse.isSuccessful) {
                        Log.w(TAG, "Nie udało się przypisać roli DENTIST: ${roleResponse.code()}")
                    }

                    // 3. Przypisz użytkownika do kliniki jako pracownika
                    val staffRequest = CreateStaffMemberRequest(
                        userId = userId,
                        firstName = fName,
                        lastName = lName,
                        profession = profession,
                        bio = bio,
                        avatarUrl = userAvatarUrl,
                        phone = phone,
                        email = email,
                        workingHoursStart = workingHoursStart,
                        workingHoursEnd = workingHoursEnd
                    )

                    val coreResponse = apiService.createStaffMember(currentTenantId, staffRequest)
                    if (coreResponse.isSuccessful) {
                        loadStaff()
                    } else {
                        Log.e(TAG, "Błąd przypisania do kliniki: ${coreResponse.code()}")
                    }
                } else {
                    Log.e(TAG, "Nie udało się uzyskać userId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek addStaff: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStaff(staffId: Long, fName: String, lName: String, profession: String, userId: Long, bio: String, workingHoursStart: String? = null, workingHoursEnd: String? = null) {
        if (!hasValidSession()) return

        viewModelScope.launch {
            try {
                val updateRequest = UpdateStaffMemberRequest(
                    userId = userId,
                    firstName = fName,
                    lastName = lName,
                    profession = profession,
                    bio = bio,
                    workingHoursStart = workingHoursStart,
                    workingHoursEnd = workingHoursEnd
                )
                val response = apiService.updateStaffMember(currentTenantId, staffId, updateRequest)
                if (response.isSuccessful) {
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
            try {
                val response = apiService.deleteStaffMember(currentTenantId, staffId)
                if (response.isSuccessful) {
                    loadStaff()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception deleteStaff: ${e.message}")
            }
        }
    }

    fun loadWorkingHours(staffId: Long, onResult: (List<StaffWorkingHoursDTO>) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.getWorkingHours(currentTenantId, staffId)
                if (response.isSuccessful) {
                    onResult(response.body() ?: emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd pobierania godzin pracy: ${e.message}")
            }
        }
    }

    fun updateWorkingHours(staffId: Long, schedule: List<WorkingHoursEntry>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val request = UpdateWorkingHoursRequest(schedule = schedule)
                val response = apiService.updateWorkingHours(currentTenantId, staffId, request)
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                Log.e(TAG, "Błąd aktualizacji godzin pracy: ${e.message}")
                onResult(false)
            }
        }
    }
}