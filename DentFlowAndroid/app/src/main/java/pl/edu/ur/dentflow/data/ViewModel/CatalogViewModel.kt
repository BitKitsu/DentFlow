package pl.edu.ur.dentflow.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.edu.ur.dentflow.data.remote.ApiService
import pl.edu.ur.dentflow.data.remote.ServiceCatalogItemDTO
import pl.edu.ur.dentflow.data.remote.ServiceCatalogRequest
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

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val TAG = "DENTFLOW_CATALOG_DEBUG"

    private val _allCatalog = mutableStateOf<List<ServiceCatalogItemDTO>>(emptyList())
    val allCatalog: State<List<ServiceCatalogItemDTO>> = _allCatalog

    private val currentTenantId: Long
        get() {
            val id = prefs.getLong("tenant_id", -1L)
            return if (id <= 0L) -1L else id
        }

    fun loadServices(id: Long = currentTenantId) {
        if (id == -1L) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getServices(id)
                when {
                    response.isSuccessful -> _servicesState.value = response.body() ?: emptyList()
                    response.code() == 403 -> {
                        _errorMessage.value = "No permission to view services."
                        Log.e(TAG, "403 Forbidden: loadServices")
                    }
                    else -> Log.e(TAG, "Error loading services: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "No connection to server."
                Log.e(TAG, "Error loading services: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllCatalog() {
        viewModelScope.launch {
            try {
                val response = apiService.getAllActiveCatalog()
                if (response.isSuccessful) {
                    _allCatalog.value = response.body() ?: emptyList()
                } else {
                    Log.e(TAG, "Error loading catalog: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading catalog: ${e.message}")
            }
        }
    }

    fun addService(name: String, priceCents: Int, duration: Int, active: Boolean = true) {
        val tId = currentTenantId
        if (tId == -1L) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = ServiceCatalogRequest(name, duration, priceCents, active)
                val response = apiService.createService(tId, request)
                if (response.isSuccessful) {
                    loadServices(tId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding service: ${e.message}")
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
                Log.e(TAG, "Error updating service: ${e.message}")
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
                    _servicesState.value = _servicesState.value.filter { it.id != serviceId }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting service: ${e.message}")
            }
        }
    }
}