package com.example.dentflow_android.data.ViewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.ApiService
import com.example.dentflow_android.data.remote.LocationRequest
import com.example.dentflow_android.data.remote.LocationResponse
import com.example.dentflow_android.data.remote.TenantRequest
import com.example.dentflow_android.data.remote.TenantResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TenantViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _tenantState = mutableStateOf<TenantResponse?>(null)
    val tenantState: State<TenantResponse?> = _tenantState

    private val TAG = "DENTFLOW_DEBUG"

    fun loadTenantData(id: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Próba pobrania danych dla tenantId: $id")
                val response = apiService.getTenantDetails(id)

                if (response.isSuccessful) {
                    _tenantState.value = response.body()
                    Log.d(TAG, "Sukces! Pobrano: ${response.body()?.name}")
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Błąd serwera: Kod $errorCode - Body: $errorBody")

                    if (errorCode == 403) {
                        Log.w(TAG, "Wykryto 403. Wstawiam dane fallback, aby odblokować UI.")
                        _tenantState.value = TenantResponse(
                            id = id,
                            name = "Klinika DentFlow (Manual)",
                            status = "ACTIVE",
                            locations = listOf(
                                LocationResponse(
                                    id = 1,
                                    tenantId = id,
                                    name = "Placówka Główna",
                                    addressStreet = "ul. Rejtana 10",
                                    addressCity = "Rzeszów",
                                    addressZip = "35-310",
                                    addressCountry = "Polska"
                                )
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek sieciowy: ${e.message}")
            }
        }
    }

    fun saveBusinessData(name: String, locName: String, street: String, city: String, zip: String) {
        viewModelScope.launch {
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
                val currentId = _tenantState.value?.id ?: 1L
                Log.d(TAG, "Wysyłanie aktualizacji dla tenantId: $currentId")
                val response = apiService.updateTenant(currentId, request)

                if (response.isSuccessful) {
                    _tenantState.value = response.body()
                    Log.d(TAG, "Zaktualizowano pomyślnie")
                } else {
                    Log.e(TAG, "Błąd zapisu: Kod ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek przy zapisie: ${e.message}")
            }
        }
    }
}
