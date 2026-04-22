package com.example.dentflow_android.data.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _visitCount = MutableStateFlow("0")
    val visitCount: StateFlow<String> = _visitCount

    private val _patientCount = MutableStateFlow("0")
    val patientCount: StateFlow<String> = _patientCount

    fun loadStats(tenantId: Long) {
        viewModelScope.launch {
            try {
                val visitsRes = apiService.getVisits(tenantId)
                if (visitsRes.isSuccessful) {
                    _visitCount.value = (visitsRes.body()?.size ?: 0).toString()
                }

                val patientsRes = apiService.getPatients(tenantId)
                if (patientsRes.isSuccessful) {
                    _patientCount.value = (patientsRes.body()?.size ?: 0).toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
