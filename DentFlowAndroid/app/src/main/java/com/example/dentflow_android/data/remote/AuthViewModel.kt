package com.example.dentflow_android.ui.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun login(request: LoginRequest, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = authService.login(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    val token = body.token

                    if (!token.isNullOrBlank()) {
                        prefs.edit().putString("jwt_token", token).apply()
                        Log.d("AUTH_DEBUG", "Token zapisany pomyślnie w SharedPreferences")
                    } else {
                        Log.e("AUTH_DEBUG", "Serwer zwrócił sukces, ale token jest pusty!")
                    }

                    onSuccess(body.tenantId)
                } else {
                    _errorMessage.value = "Błędny e-mail lub hasło"
                    Log.e("AUTH_DEBUG", "Błąd logowania: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd połączenia z serwerem"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(request: RegisterRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = authService.register(request)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _errorMessage.value = "Rejestracja nieudana. Email może być zajęty."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd sieci: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        prefs.edit().remove("jwt_token").apply()
        Log.d("AUTH_DEBUG", "Token usunięty - wylogowano.")
    }
}
