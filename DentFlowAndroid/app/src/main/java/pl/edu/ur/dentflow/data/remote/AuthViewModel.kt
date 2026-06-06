package pl.edu.ur.dentflow.data.remote

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionState(
    val email: String = "",
    val role: String = "STAFF",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val addressStreet: String = "",
    val addressCity: String = "",
    val addressZip: String = "",
    val addressCountry: String = "",
    val avatarUrl: String = "",
    val tenantId: Long = 0L,
    val userId: Long = 0L
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val apiService: ApiService,
    private val prefs: SharedPreferences
) : ViewModel() {

    private fun loadSessionState(): SessionState {
        return SessionState(
            email = prefs.getString("user_email", "") ?: "",
            role = prefs.getString("user_role", "STAFF") ?: "STAFF",
            firstName = prefs.getString("user_first_name", "") ?: "",
            lastName = prefs.getString("user_last_name", "") ?: "",
            phone = prefs.getString("user_phone", "") ?: "",
            addressStreet = prefs.getString("user_addr_street", "") ?: "",
            addressCity = prefs.getString("user_addr_city", "") ?: "",
            addressZip = prefs.getString("user_addr_zip", "") ?: "",
            addressCountry = prefs.getString("user_addr_country", "") ?: "",
            avatarUrl = prefs.getString("user_avatar_url", "") ?: "",
            tenantId = prefs.getLong("tenant_id", 0L),
            userId = prefs.getLong("user_id", 0L)
        )
    }

    private val _sessionState = MutableStateFlow(loadSessionState())
    val sessionState: StateFlow<SessionState> = _sessionState

    fun refreshSession() {
        val jwt = prefs.getString("jwt_token", null)
        if (!jwt.isNullOrBlank()) {
            val role = decodeRoleFromJwt(jwt)
            prefs.edit().putString("user_role", role).apply()
        }
        _sessionState.value = loadSessionState()
    }


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
                        val role = decodeRoleFromJwt(token)
                        saveSession(body, role)
                        Log.d("AUTH_DEBUG", "Login successful. TenantID: ${body.tenantId}, Role: $role")
                        onSuccess(body.tenantId)
                    } else {
                        _errorMessage.value = "Błąd: Serwer nie przesłał tokenu."
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AUTH_DEBUG", "Login error. Code: ${response.code()}, Body: $errorBody")
                    _errorMessage.value = when (response.code()) {
                        401 -> "Błędny e-mail lub hasło."
                        403 -> "Dostęp zabroniony. Sprawdź konfigurację kliniki."
                        500 -> "Błąd wewnętrzny serwera."
                        else -> "Błąd autoryzacji: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd połączenia. Upewnij się, że backend działa."
                Log.e("AUTH_DEBUG", "Login exception: ${e.message}")
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
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val role = decodeRoleFromJwt(body.token)
                    saveSession(body, role)
                    Log.d("AUTH_DEBUG", "Registration successful")
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AUTH_DEBUG", "Registration error. Code: ${response.code()}, Body: $errorBody")
                    _errorMessage.value = when (response.code()) {
                        409 -> "Ten adres e-mail jest już zarejestrowany."
                        400 -> "Nieprawidłowe dane w formularzu."
                        else -> "Rejestracja odrzucona (Kod: ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd sieci: Brak odpowiedzi od serwera."
                Log.e("AUTH_DEBUG", "Registration exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
        _sessionState.value = SessionState()
        Log.d("AUTH_DEBUG", "Logged out, session data cleared locally.")
        viewModelScope.launch {
            try {
                authService.logout()
            } catch (e: Exception) {
                Log.e("AUTH_DEBUG", "Logout API error: ${e.message}")
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String,
                       onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = authService.changePassword(
                    ChangePasswordRequest(currentPassword, newPassword)
                )
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError(when (response.code()) {
                        401 -> "Obecne hasło jest nieprawidłowe."
                        400 -> "Nowe hasło musi mieć co najmniej 8 znaków."
                        else -> "Błąd zmiany hasła (${response.code()})"
                    })
                }
            } catch (e: Exception) {
                onError("Błąd połączenia z serwerem.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = authService.deleteAccount()
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Błąd usuwania konta (${response.code()})")
                }
            } catch (e: Exception) {
                onError("Błąd połączenia z serwerem.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(
        firstName: String?,
        lastName: String?,
        phone: String?,
        email: String?,
        addressStreet: String?,
        addressCity: String?,
        addressZip: String?,
        addressCountry: String?,
        avatarUrl: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = authService.updateProfile(
                    UpdateProfileRequest(
                        email          = email?.takeIf { it.isNotBlank() },
                        firstName      = firstName,
                        lastName       = lastName,
                        phone          = phone,
                        addressStreet  = addressStreet,
                        addressCity    = addressCity,
                        addressZip     = addressZip,
                        addressCountry = addressCountry,
                        avatarUrl      = avatarUrl
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val role = decodeRoleFromJwt(body.token)
                    saveSession(body, role)
                    Log.d("AUTH_DEBUG", "Profile updated successfully")
                    
                    // Sync staff member data
                    val tenantId = body.tenantId
                    if (tenantId > 0) {
                        try {
                            apiService.syncStaffFromUser(
                                tenantId,
                                SyncFromUserRequest(
                                    userId = body.userId,
                                    firstName = firstName,
                                    lastName = lastName,
                                    avatarUrl = avatarUrl,
                                    phone = phone,
                                    email = email
                                )
                            )
                        } catch (e: Exception) {
                            Log.w("AUTH_DEBUG", "Staff sync failed: ${e.message}")
                        }
                    }
                    
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    onError(when (response.code()) {
                        409 -> "Podany adres e-mail jest już zajęty."
                        400 -> "Nieprawidłowe dane. Sprawdź formularz."
                        401 -> "Sesja wygasła. Zaloguj się ponownie."
                        else -> "Błąd aktualizacji (${response.code()}): $errorBody"
                    })
                }
            } catch (e: Exception) {
                onError("Błąd połączenia z serwerem.")
                Log.e("AUTH_DEBUG", "updateProfile exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveSession(body: AuthResponse, role: String) {
        prefs.edit().apply {
            putString("jwt_token",        body.token)
            putLong("tenant_id",          body.tenantId)
            putLong("user_id",            body.userId)
            putString("user_role",        role)
            putString("user_email",       body.email)
            putString("user_first_name",  body.firstName     ?: "")
            putString("user_last_name",   body.lastName      ?: "")
            putString("user_phone",       body.phone         ?: "")
            putString("user_addr_street", body.addressStreet ?: "")
            putString("user_addr_city",   body.addressCity   ?: "")
            putString("user_addr_zip",    body.addressZip    ?: "")
            putString("user_addr_country",body.addressCountry?: "")
            putString("user_avatar_url",  body.avatarUrl     ?: "")
            apply()
        }
        _sessionState.value = loadSessionState()
    }

    private fun decodeRoleFromJwt(token: String): String {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return "PATIENT"
            val payload = parts[1]
            val decoded = android.util.Base64.decode(
                payload.replace('-', '+').replace('_', '/'),
                android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
            )
            val json = String(decoded, Charsets.UTF_8)
            val knownRoles = listOf("OWNER", "DENTIST", "RECEPTIONIST", "ASSISTANT", "PATIENT")
            val foundRoles = knownRoles.filter { role -> json.contains("\"$role\"") }
            val priority = listOf("OWNER", "DENTIST", "RECEPTIONIST", "ASSISTANT", "PATIENT")
            priority.firstOrNull { it in foundRoles } ?: "PATIENT"
        } catch (e: Exception) {
            Log.e("AUTH_DEBUG", "JWT decode error: ${e.message}")
            "PATIENT"
        }
    }
}