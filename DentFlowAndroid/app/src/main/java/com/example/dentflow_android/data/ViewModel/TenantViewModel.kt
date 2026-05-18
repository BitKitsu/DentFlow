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
            Log.d(TAG, "Odczytano tenant_id z SharedPreferences: $id")
            return if (id <= 0L) -1L else id
        }

    fun loadAllTenantData() {
        val id = currentTenantId
        if (id == -1L) {
            Log.e(TAG, "loadAllTenantData: Brak kliniki (id <= 0). Przerywam pobieranie danych.")
            _tenantState.value = null
            return
        }

        Log.d(TAG, "Rozpoczynam sekwencyjne pobieranie wszystkich danych dla kliniki ID: $id")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                fetchTenantData(id)
                fetchServices(id)
                fetchRooms(id)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchTenantData(id: Long) {
        try {
            Log.d(TAG, "API -> Pobieranie danych kliniki (getTenantDetails) dla ID: $id")
            val response = apiService.getTenantDetails(id)
            if (response.isSuccessful) {
                Log.d(TAG, "API -> Pobrano szczegóły kliniki pomyślnie.")
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
            Log.d(TAG, "Rejestracja nowej kliniki: $name, Miejscowość: $city")
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
                    Log.d(TAG, "API -> Zarejestrowano klinikę. Nowe ID: ${newTenant.id}")
                    prefs.edit().putLong("tenant_id", newTenant.id).apply()
                    assignTenantOnIdentityService(newTenant.id)
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

    private fun assignTenantOnIdentityService(tenantId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "API -> Przypisywanie kliniki w IdentityService dla ID: $tenantId")
                val response = authService.assignTenant(AssignTenantRequest(tenantId = tenantId))
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    Log.d(TAG, "API -> Przypisano klinikę. Nowy token JWT wygenerowany.")
                    prefs.edit().putString("jwt_token", authResponse.token)
                        .putLong("tenant_id", authResponse.tenantId)
                        .apply()
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e(TAG, "API BŁĄD -> assignTenant: Kod=${response.code()}, Body=$errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "WYJĄTEK przy assignTenant: ${e.message}", e)
            }
        }
    }

    fun loadServices(id: Long = currentTenantId) {
        if (id == -1L) {
            Log.e(TAG, "loadServices anulowane: Brak poprawnego ID kliniki (-1L)")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                fetchServices(id)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchServices(id: Long) {
        try {
            Log.d(TAG, "API -> Pobieranie listy usług (getServices) dla kliniki: $id")
            val response = apiService.getServices(id)
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                Log.d(TAG, "API -> Pobrano pomyślnie ${list.size} usług.")
                _servicesState.value = list
            } else {
                val errorMsg = response.errorBody()?.string()
                Log.e(TAG, "API BŁĄD -> getServices: Kod=${response.code()}, Body=$errorMsg")
            }
        } catch (e: Exception) {
            Log.e(TAG, "WYJĄTEK przy pobieraniu usług cennika: ${e.message}", e)
        }
    }

    fun addService(name: String, priceCents: Int, duration: Int) {
        val tId = currentTenantId
        if (tId == -1L) {
            Log.e(TAG, "addService anulowane: Nieprawidłowe ID kliniki (-1L)")
            return
        }

        Log.d(TAG, "Żądanie DODANIA usługi -> Name: $name, PriceCents: $priceCents, Duration: $duration min, Tenant: $tId")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = ServiceCatalogRequest(name, duration, priceCents, true)
                val response = apiService.createService(tId, request)
                if (response.isSuccessful) {
                    Log.d(TAG, "API -> Pomyślnie dodano usługę na backendzie. Odświeżam listę cennika.")
                    fetchServices(tId)
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e(TAG, "API BŁĄD -> createService: Kod=${response.code()}, Body=$errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "WYJĄTEK przy dodawaniu usługi: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateService(serviceId: Long, name: String, priceCents: Int, duration: Int, active: Boolean) {
        val tId = currentTenantId
        if (tId == -1L) {
            Log.e(TAG, "updateService anulowane: Nieprawidłowe ID kliniki (-1L)")
            return
        }

        Log.d(TAG, "Żądanie EDYCJI usługi ID: $serviceId -> Name: $name, PriceCents: $priceCents, Duration: $duration min, Active: $active")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = ServiceCatalogRequest(name, duration, priceCents, active)
                val response = apiService.updateService(tId, serviceId, request)
                if (response.isSuccessful) {
                    Log.d(TAG, "API -> Pomyślnie zaktualizowano usługę ID: $serviceId. Odświeżam listę.")
                    fetchServices(tId)
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e(TAG, "API BŁĄD -> updateService: Kod=${response.code()}, Body=$errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "WYJĄTEK przy edycji usługi: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteService(serviceId: Long) {
        val tId = currentTenantId
        if (tId == -1L) {
            Log.e(TAG, "deleteService anulowane: Nieprawidłowe ID kliniki (-1L)")
            return
        }

        Log.d(TAG, "Żądanie USUNIĘCIA usługi ID: $serviceId dla kliniki: $tId")
        viewModelScope.launch {
            try {
                val response = apiService.deleteService(tId, serviceId)
                if (response.isSuccessful) {
                    Log.d(TAG, "API -> Usunięto pomyślnie z bazy. Usuwam z lokalnej listy stanów.")
                    _servicesState.value = _servicesState.value.filter { it.id != serviceId }
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e(TAG, "API BŁĄD -> deleteService: Kod=${response.code()}, Body=$errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "WYJĄTEK przy usuwaniu usługi: ${e.message}", e)
            }
        }
    }

    fun loadRooms(id: Long) {
        viewModelScope.launch {
            fetchRooms(id)
        }
    }

    private suspend fun fetchRooms(id: Long) {
        try {
            Log.d(TAG, "API -> Pobieranie pokoi/gabinetów dla ID: $id")
            val res = apiService.getRooms(id)
            if (res.isSuccessful) {
                Log.d(TAG, "API -> Pobrano pokoje pomyślnie.")
                _rooms.value = res.body() ?: emptyList()
            } else {
                val errorMsg = res.errorBody()?.string()
                Log.e(TAG, "API BŁĄD -> getRooms: Kod=${res.code()}, Body=$errorMsg")
            }
        } catch (e: Exception) {
            Log.e(TAG, "WYJĄTEK przy pobieraniu pokoi: ${e.message}", e)
        }
    }

    fun saveBusinessData(name: String, locName: String, street: String, city: String, zip: String) {
        val id = prefs.getLong("tenant_id", 0L)
        Log.d(TAG, "Aktualizacja danych biznesowych klini ID: $id")
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
                    Log.d(TAG, "API -> Zaktualizowano dane biznesowe pomyślnie.")
                    _tenantState.value = response.body()
                    response.body()?.id?.let {
                        prefs.edit().putLong("tenant_id", it).apply()
                    }
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e(TAG, "API BŁĄD -> updateTenant: Kod=${response.code()}, Body=$errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "WYJĄTEK przy aktualizacji danych biznesowych: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}