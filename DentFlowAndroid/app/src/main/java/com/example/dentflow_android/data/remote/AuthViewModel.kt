package com.example.dentflow_android.data.remote

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
                        prefs.edit().apply {
                            putString("jwt_token", token)
                            putLong("tenant_id", body.tenantId)
                            putLong("user_id", body.userId)
                            apply()
                        }
                        Log.d("AUTH_DEBUG", "Zalogowano pomyślnie. TenantID: ${body.tenantId}")
                        Log.d("AUTH_DEBUG", "PELNY TOKEN: $token")
                        onSuccess(body.tenantId)
                    } else {
                        _errorMessage.value = "Błąd: Serwer nie przesłał tokenu."
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AUTH_DEBUG", "Błąd logowania. Kod: ${response.code()}, Body: $errorBody")

                    _errorMessage.value = when (response.code()) {
                        401 -> "Błędny e-mail lub hasło."
                        403 -> "Dostęp zabroniony. Sprawdź konfigurację kliniki."
                        500 -> "Błąd wewnętrzny serwera. Sprawdź logi bazy danych."
                        else -> "Błąd autoryzacji: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd połączenia. Upewnij się, że backend działa."
                Log.e("AUTH_DEBUG", "Wyjątek podczas logowania: ${e.message}")
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
                    Log.d("AUTH_DEBUG", "Rejestracja zakończona sukcesem")
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AUTH_DEBUG", "Błąd rejestracji. Kod: ${response.code()}, Body: $errorBody")

                    _errorMessage.value = when (response.code()) {
                        409 -> "Ten adres e-mail jest już zarejestrowany."
                        400 -> "Nieprawidłowe dane w formularzu."
                        else -> "Rejestracja odrzucona (Kod: ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd sieci: Brak odpowiedzi od serwera."
                Log.e("AUTH_DEBUG", "Wyjątek podczas rejestracji: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authService.logout()
            } catch (e: Exception) {
                Log.e("AUTH_DEBUG", "Błąd przy wysyłaniu logout do API: ${e.message}")
            } finally {
                prefs.edit().apply {
                    remove("jwt_token")
                    remove("tenant_id")
                    remove("user_id")
                    apply()
                }
                Log.d("AUTH_DEBUG", "Wylogowano lokalnie i wyczyszczono dane sesji.")
            }
        }
    }
}