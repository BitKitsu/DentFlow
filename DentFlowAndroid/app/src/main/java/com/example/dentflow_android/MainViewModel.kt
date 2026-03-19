package com.example.dentflow_android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    fun fetchData() {
        viewModelScope.launch {
            try {
                val users = apiService.getUsers()
                println("Pobrano użytkowników: ${users.size}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}