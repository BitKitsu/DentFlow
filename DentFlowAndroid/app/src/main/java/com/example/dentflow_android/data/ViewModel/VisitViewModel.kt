package com.example.dentflow_android.data.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VisitWithPatient(
    val visit: VisitResponse,
    val patient: PatientResponse?
)

@HiltViewModel
class VisitViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _visits = MutableStateFlow<List<VisitWithPatient>>(emptyList())
    val visits: StateFlow<List<VisitWithPatient>> = _visits

    init {
        fetchVisitsWithPatients(tenantId = 1L)
    }

    private fun fetchVisitsWithPatients(tenantId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.getVisits(tenantId)

                if (response.isSuccessful) {
                    val visitList = response.body() ?: emptyList()

                    val combinedList = visitList.map { visit ->
                        val patientResponse = apiService.getPatientById(tenantId, visit.patientId)
                        VisitWithPatient(
                            visit = visit,
                            patient = if (patientResponse.isSuccessful) patientResponse.body() else null
                        )
                    }

                    _visits.value = combinedList
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
