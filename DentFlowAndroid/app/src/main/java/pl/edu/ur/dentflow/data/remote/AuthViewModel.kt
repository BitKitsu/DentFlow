package pl.edu.ur.dentflow.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.edu.ur.dentflow.R
import javax.inject.Inject

private const val TAG = "AuthViewModel"

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
    private val prefs: SharedPreferences,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
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

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "jwt_token") {
            refreshSession()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

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

                        if (body.tenantId <= 0L && role in listOf("OWNER", "DENTIST", "RECEPTIONIST", "ASSISTANT")) {
                            syncTenantFromStaffRecords(body.userId, role)
                        }

                        onSuccess(body.tenantId)
                    } else {
                        _errorMessage.value = context.getString(R.string.error_server_no_token)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Login error. Code: ${response.code()}, Body: $errorBody")
                    _errorMessage.value = when (response.code()) {
                        401 -> context.getString(R.string.error_wrong_credentials)
                        403 -> context.getString(R.string.error_forbidden)
                        500 -> context.getString(R.string.error_server_internal)
                        else -> context.getString(R.string.error_auth, response.code())
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.error_connection_backend)
                Log.e(TAG, "Login exception: ${e.message}")
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
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Registration error. Code: ${response.code()}, Body: $errorBody")
                    _errorMessage.value = when (response.code()) {
                        409 -> context.getString(R.string.error_email_taken)
                        400 -> context.getString(R.string.error_invalid_form)
                        else -> context.getString(R.string.error_registration, response.code())
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.error_network)
                Log.e(TAG, "Registration exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
        _sessionState.value = SessionState()
        viewModelScope.launch {
            try {
                authService.logout()
            } catch (e: Exception) {
                Log.e(TAG, "Logout API error: ${e.message}")
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
                        401 -> context.getString(R.string.error_wrong_current_password)
                        400 -> context.getString(R.string.error_password_min)
                        else -> context.getString(R.string.error_password_change, response.code())
                    })
                }
            } catch (e: Exception) {
                onError(context.getString(R.string.error_connection_server))
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
                    onError(context.getString(R.string.error_delete_account, response.code()))
                }
            } catch (e: Exception) {
                onError(context.getString(R.string.error_connection_server))
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

                        if (role == "PATIENT") {
                            try {
                                apiService.ensurePatient(
                                    tenantId = tenantId,
                                    userId = body.userId,
                                    firstName = firstName ?: "",
                                    lastName = lastName ?: "",
                                    email = email ?: "",
                                    phone = phone ?: ""
                                )
                            } catch (e: Exception) {
                                Log.w("AUTH_DEBUG", "Patient sync failed: ${e.message}")
                            }
                        }
                    }
                    
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    onError(when (response.code()) {
                        409 -> context.getString(R.string.error_email_occupied)
                        400 -> context.getString(R.string.error_invalid_data)
                        401 -> context.getString(R.string.error_session_expired)
                        else -> context.getString(R.string.error_profile_update, response.code(), errorBody ?: "")
                    })
                }
            } catch (e: Exception) {
                onError(context.getString(R.string.error_connection_server))
                Log.e(TAG, "updateProfile exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveSession(body: AuthResponse, role: String) {
        val existingTenantId = prefs.getLong("tenant_id", 0L)
        prefs.edit().apply {
            putString("jwt_token",        body.token)
            if (body.tenantId > 0L) {
                putLong("tenant_id",      body.tenantId)
            } else if (existingTenantId > 0L) {
                // Preserve existing tenantId (e.g. patient who already selected a clinic)
            } else {
                remove("tenant_id")
            }
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
            Log.e(TAG, "JWT decode error: ${e.message}")
            "PATIENT"
        }
    }

    private suspend fun syncTenantFromStaffRecords(userId: Long, role: String) {
        try {
            val response = apiService.getAllStaffMembers()
            if (response.isSuccessful) {
                val staff = response.body() ?: emptyList()
                val match = staff.find { it.userId == userId }
                if (match != null && match.tenantId > 0L) {
                    prefs.edit().putLong("tenant_id", match.tenantId).apply()
                    Log.i(TAG, "Synced tenantId=${match.tenantId} for userId=$userId from staff records")
                    refreshSession()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync tenant from staff records: ${e.message}")
        }
    }
}