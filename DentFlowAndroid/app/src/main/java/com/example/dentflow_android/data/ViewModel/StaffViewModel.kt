package com.example.dentflow_android.data.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.ApiService
import com.example.dentflow_android.data.remote.StaffMemberResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _staffMembers = MutableStateFlow<List<StaffMemberResponse>>(emptyList())
    val staffMembers: StateFlow<List<StaffMemberResponse>> = _staffMembers

    fun loadStaff(tenantId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.getStaff(tenantId)
                if (response.isSuccessful) {
                    _staffMembers.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
