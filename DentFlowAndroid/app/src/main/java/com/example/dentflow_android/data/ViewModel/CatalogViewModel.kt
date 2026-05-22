package com.example.dentflow_android.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.ApiService
import com.example.dentflow_android.data.remote.ServiceCatalogItemDTO
import com.example.dentflow_android.data.remote.ServiceCatalogRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val apiService: ApiService,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _servicesState = mutableStateOf<List<ServiceCatalogItemDTO>>(emptyList())
    val servicesState: State<List<ServiceCatalogItemDTO>> = _servicesState

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val TAG = "DENTFLOW_CATALOG_DEBUG"

    private val currentTenantId: Long
        get() {
            val id = prefs.getLong("tenant_id", -1L)
            return if (id <= 0L) -1L else id
        }

    fun loadServices(id: Long = currentTenantId) {
        if (id == -1L) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getServices(id)
                if (response.isSuccessful) {
                    _servicesState.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd pobierania usług: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addService(name: String, priceCents: Int, duration: Int) {
        val tId = currentTenantId
        if (tId == -1L) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = ServiceCatalogRequest(name, duration, priceCents, true)
                val response = apiService.createService(tId, request)
                if (response.isSuccessful) {
                    loadServices(tId) // Odśwież listę po sukcesie
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd dodawania usługi: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateService(serviceId: Long, name: String, priceCents: Int, duration: Int, active: Boolean) {
        val tId = currentTenantId
        if (tId == -1L) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = ServiceCatalogRequest(name, duration, priceCents, active)
                val response = apiService.updateService(tId, serviceId, request)
                if (response.isSuccessful) {
                    loadServices(tId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd edycji usługi: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteService(serviceId: Long) {
        val tId = currentTenantId
        if (tId == -1L) return

        viewModelScope.launch {
            try {
                val response = apiService.deleteService(tId, serviceId)
                if (response.isSuccessful) {
                    // Szybkie usunięcie z lokalnego stanu UI
                    _servicesState.value = _servicesState.value.filter { it.id != serviceId }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd usuwania usługi: ${e.message}")
            }
        }
    }
}