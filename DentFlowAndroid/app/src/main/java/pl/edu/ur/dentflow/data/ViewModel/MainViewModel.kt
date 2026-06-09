package pl.edu.ur.dentflow.data.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.edu.ur.dentflow.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    fun fetchData(tenantId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.getStaffMembers(tenantId)

                if (!response.isSuccessful) {
                    Log.e(TAG, "Błąd serwera: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchData error", e)
            }
        }
    }
}