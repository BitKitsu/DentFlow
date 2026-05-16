package com.example.dentflow_android.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.*
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

    private val _servicesState = mutableStateOf<List<ServiceCatalogItemDTO>>(emptyList())
    val servicesState: State<List<ServiceCatalogItemDTO>> = _servicesState

    private val _rooms = MutableStateFlow<List<RoomResponse>>(emptyList())
    val rooms = _rooms.asStateFlow()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val TAG = "DENTFLOW_DEBUG"

    private val currentTenantId: Long
        get() {
            val id = prefs.getLong("tenant_id", -1L)
            return if (id <= 0L) -1L else id
        }

    fun loadAllTenantData() {
        val id = currentTenantId
        if (id == -1L) {
            Log.d(TAG, "Brak kliniki (id <= 0). Pokazuję ekran rejestracji.")
            _tenantState.value = null
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                loadTenantData(id)
                loadServices(id)
                loadRooms(id)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTenantData(id: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.getTenantDetails(id)
                if (response.isSuccessful) {
                    _tenantState.value = response.body()
                } else {
                    Log.e(TAG, "Błąd pobierania: ${response.code()}")
                    if (response.code() == 403) _tenantState.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek sieciowy: ${e.message}")
            }
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
                val token = prefs.getString("token", "Brak tokenu")
                Log.d("DENTFLOW_TOKEN", "Mój token to: Bearer $token")
                val response = apiService.registerTenant(request)

                if (response.isSuccessful && response.body() != null) {
                    val newTenant = response.body()!!
                    prefs.edit().putLong("tenant_id", newTenant.id).apply()
                    assignTenantOnIdentityService(newTenant.id)
                    _tenantState.value = newTenant
                    loadAllTenantData()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Błąd rejestracji: ${response.code()}, Body: $errorBody")

                    if (response.code() == 403) {
                        saveBusinessData(name, locationName, street, city, zip)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek przy rejestracji: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveBusinessData(name: String, locName: String, street: String, city: String, zip: String) {
        val id = prefs.getLong("tenant_id", 0L)

        viewModelScope.launch {
            _isLoading.value = true
            val request = TenantRequest(
                name = name,
                location = LocationRequest(
                    name = locName,
                    addressStreet = street,
                    addressCity = city,
                    addressZip = zip,
                    addressCountry = "Polska"
                )
            )
            try {
                val response = apiService.updateTenant(id, request)
                if (response.isSuccessful) {
                    _tenantState.value = response.body()
                    response.body()?.id?.let {
                        prefs.edit().putLong("tenant_id", it).apply()
                    }
                } else {
                    Log.e(TAG, "Błąd edycji: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd edycji: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    private fun assignTenantOnIdentityService(tenantId: Long) {
        viewModelScope.launch {
            try {
                val response = authService.assignTenant(AssignTenantRequest(tenantId = tenantId))
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    prefs.edit().putString("jwt_token", authResponse.token)
                        .putLong("tenant_id", authResponse.tenantId)
                        .apply()
                } else {
                    // log error
                }
            } catch (e: Exception) {
                // log error
            }
        }
    }
    
    fun loadServices(id: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.getServices(id)
                if (response.isSuccessful) {
                    _servicesState.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd usług: ${e.message}")
            }
        }
    }

    fun loadRooms(id: Long) {
        viewModelScope.launch {
            try {
                val res = apiService.getRooms(id)
                if (res.isSuccessful) {
                    _rooms.value = res.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd pokoi: ${e.message}")
            }
        }
    }
}