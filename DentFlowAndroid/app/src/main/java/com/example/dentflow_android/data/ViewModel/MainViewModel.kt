package com.example.dentflow_android.data.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    fun fetchData(tenantId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.getStaff(tenantId)

                if (response.isSuccessful) {
                    val staffMembers = response.body()
                    println("Pobrano personel: ${staffMembers?.size ?: 0}")
                } else {
                    println("Błąd serwera: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
