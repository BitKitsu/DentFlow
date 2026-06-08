package pl.edu.ur.dentflow.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.edu.ur.dentflow.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TenantViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authService: AuthService,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _tenantState = mutableStateOf<TenantResponse?>(null)
    val tenantState: State<TenantResponse?> = _tenantState

    private val _rooms = MutableStateFlow<List<RoomResponse>>(emptyList())
    val rooms = _rooms.asStateFlow()

    private val _allTenants = mutableStateOf<List<TenantResponse>>(emptyList())
    val allTenants: State<List<TenantResponse>> = _allTenants

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val TAG = "DENTFLOW_DEBUG"

    val currentTenantId: Long
        get() {
            val id = prefs.getLong("tenant_id", -1L)
            return if (id <= 0L) -1L else id
        }

    private val currentUserId: Long
        get() = prefs.getLong("user_id", 0L)

    fun loadAllTenantData() {
        val id = currentTenantId
        if (id == -1L) {
            Log.e(TAG, "loadAllTenantData: Brak kliniki (id <= 0). Przerywam pobieranie danych.")
            _tenantState.value = null
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                fetchTenantData(id)
                fetchRooms(id)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllTenants() {
        viewModelScope.launch {
            try {
                val response = apiService.getAllTenants()
                if (response.isSuccessful) {
                    _allTenants.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd pobierania wszystkich klinik: ${e.message}")
            }
        }
    }

    private suspend fun fetchTenantData(id: Long) {
        try {
            val response = apiService.getTenantDetails(id)
            if (response.isSuccessful) {
                _tenantState.value = response.body()
            } else {
                val errorMsg = response.errorBody()?.string()
                Log.e(TAG, "API BŁĄD -> getTenantDetails: Kod=${response.code()}, Body=$errorMsg")
                if (response.code() == 403) _tenantState.value = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "WYJĄTEK sieciowy przy pobieraniu kliniki: ${e.message}", e)
        }
    }

    fun registerClinic(
        name: String,
        locationName: String,
        street: String,
        city: String,
        zip: String,
        country: String = "Polska",
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            prefs.edit().remove("tenant_id").apply()

            val request = RegisterTenantRequest(
                name = name,
                locationName = locationName,
                addressStreet = street,
                addressCity = city,
                addressZip = zip,
                addressCountry = country
            )

            try {
                val response = apiService.registerTenant(request)
                if (response.isSuccessful && response.body() != null) {
                    val newTenant = response.body()!!
                    prefs.edit().putLong("tenant_id", newTenant.id).apply()

                    assignTenantOnIdentityService(newTenant.id)
                    assignOwnerRole()
                    _tenantState.value = newTenant
                    loadAllTenantData()
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e(TAG, "API BŁĄD -> registerTenant: Kod=${response.code()}, Body=$errorMsg")
                    if (response.code() == 403) {
                        saveBusinessData(name, locationName, street, city, zip)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "WYJĄTEK przy rejestracji kliniki: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun assignTenantOnIdentityService(tenantId: Long) {
        try {
            val response = authService.assignTenant(AssignTenantRequest(tenantId = tenantId))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                prefs.edit()
                    .putString("jwt_token", authResponse.token)
                    .putLong("tenant_id", authResponse.tenantId)
                    .commit()
            } else {
                val errorMsg = response.errorBody()?.string()
                Log.e(TAG, "API BŁĄD -> assignTenant: Kod=${response.code()}, Body=$errorMsg")
            }
        } catch (e: Exception) {
            Log.e(TAG, "WYJĄTEK przy assignTenant: ${e.message}", e)
        }
    }

    private suspend fun assignOwnerRole() {
        val userId = currentUserId
        if (userId <= 0L) {
            Log.w(TAG, "assignOwnerRole: Brak userId, pomijam")
            return
        }
        try {
            val response = authService.assignRole(AssignRoleRequest(userId = userId, role = "OWNER"))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // Save new JWT with updated roles
                prefs.edit()
                    .putString("jwt_token", authResponse.token)
                    .putLong("tenant_id", authResponse.tenantId)
                    .apply()
            } else {
                val errorMsg = response.errorBody()?.string()
                Log.e(TAG, "API BŁĄD -> assignRole: Kod=${response.code()}, Body=$errorMsg")
            }
        } catch (e: Exception) {
            Log.e(TAG, "WYJĄTEK przy assignRole: ${e.message}")
        }
    }

    fun loadRooms(id: Long) {
        viewModelScope.launch {
            fetchRooms(id)
        }
    }

    private suspend fun fetchRooms(id: Long) {
        try {
            val res = apiService.getRooms(id)
            if (res.isSuccessful) {
                _rooms.value = res.body() ?: emptyList()
            } else {
                val errorMsg = res.errorBody()?.string()
                Log.e(TAG, "API BŁĄD -> getRooms: Kod=${res.code()}, Body=$errorMsg")
            }
        } catch (e: Exception) {
            Log.e(TAG, "WYJĄTEK przy pobieraniu pokoi: ${e.message}", e)
        }
    }

    fun createRoom(name: String, locationId: Long) {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

        viewModelScope.launch {
            try {
                val response = apiService.createRoom(tenantId, name, locationId)
                if (response.isSuccessful) {
                    fetchRooms(tenantId)
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e(TAG, "API error createRoom: ${response.code()} $errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception createRoom: ${e.message}", e)
            }
        }
    }

    fun updateRoom(roomId: Long, name: String, locationId: Long) {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

        viewModelScope.launch {
            try {
                val response = apiService.updateRoom(tenantId, roomId, name, locationId)
                if (response.isSuccessful) {
                    fetchRooms(tenantId)
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e(TAG, "API error updateRoom: ${response.code()} $errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception updateRoom: ${e.message}", e)
            }
        }
    }

    fun deleteRoom(roomId: Long) {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

        viewModelScope.launch {
            try {
                val response = apiService.deleteRoom(tenantId, roomId)
                if (response.isSuccessful) {
                    fetchRooms(tenantId)
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e(TAG, "API error deleteRoom: ${response.code()} $errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception deleteRoom: ${e.message}", e)
            }
        }
    }

    fun assignStaffToRoom(roomId: Long, staffId: Long) {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

        viewModelScope.launch {
            try {
                val response = apiService.assignStaffToRoom(tenantId, roomId, staffId)
                if (response.isSuccessful) {
                    fetchRooms(tenantId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception assignStaffToRoom: ${e.message}", e)
            }
        }
    }

    fun removeStaffFromRoom(roomId: Long, staffId: Long) {
        val tenantId = currentTenantId
        if (tenantId == -1L) return

        viewModelScope.launch {
            try {
                val response = apiService.removeStaffFromRoom(tenantId, roomId, staffId)
                if (response.isSuccessful) {
                    fetchRooms(tenantId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception removeStaffFromRoom: ${e.message}", e)
            }
        }
    }

    fun saveBusinessData(name: String, locName: String, street: String, city: String, zip: String, logoUrl: String? = null) {
        val id = prefs.getLong("tenant_id", 0L)
        viewModelScope.launch {
            _isLoading.value = true
            val request = TenantRequest(
                name = name,
                logoUrl = logoUrl,
                locationName = locName,
                addressStreet = street,
                addressCity = city,
                addressZip = zip,
                addressCountry = "Polska"
            )
            try {
                val response = apiService.updateTenant(id, request)
                if (response.isSuccessful) {
                    _tenantState.value = response.body()
                    response.body()?.id?.let {
                        prefs.edit().putLong("tenant_id", it).commit()
                    }
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e(TAG, "API BŁĄD -> updateTenant: Kod=${response.code()}, Body=$errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "WYJATEK przy aktualizacji danych biznesowych: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteClinic(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val id = prefs.getLong("tenant_id", 0L)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.deleteTenant(id)
                if (response.isSuccessful) {
                    prefs.edit().remove("tenant_id").apply()
                    _tenantState.value = null
                    onSuccess()
                } else {
                    val raw = response.errorBody()?.string() ?: ""
                    Log.e(TAG, "API BŁĄD -> deleteTenant: ${response.code()} $raw")
                    val msg = parseErrorMessage(raw)
                        ?: "Nie udało się usunąć kliniki (${response.code()})"
                    onError(msg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "WYJĄTEK przy usuwaniu kliniki: ${e.message}", e)
                onError(e.message ?: "Błąd połączenia")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseErrorMessage(raw: String): String? {
        if (raw.isBlank()) return null
        if (raw.startsWith("<!")) return null
        return try {
            val jsonObj = org.json.JSONObject(raw)
            jsonObj.optString("message").takeIf { it.isNotBlank() }
                ?: jsonObj.optString("error").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            raw.takeIf { it.length < 200 }
        }
    }
}
