package pl.edu.ur.dentflow.data.ViewModel

import android.util.Log
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.edu.ur.dentflow.data.remote.ApiService
import pl.edu.ur.dentflow.data.remote.NotificationDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "NotificationViewModel"

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationDTO>>(emptyList())
    val notifications: StateFlow<List<NotificationDTO>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun getSessionData(): Pair<Long, Long> {
        val tenantId = sharedPrefs.getLong("tenant_id", -1L)
        val userId   = sharedPrefs.getLong("user_id",   -1L)
        return Pair(tenantId, userId)
    }

    // ─── Pobierz wszystkie powiadomienia + licznik nieprzeczytanych ───────────

    fun fetchNotifications() {
        val (tenantId, userId) = getSessionData()
        if (tenantId == -1L || userId == -1L) {
            _errorMessage.value = "Brak danych sesji. Zaloguj się ponownie."
            return
        }

        viewModelScope.launch {
            _isLoading.value   = true
            _errorMessage.value = null
            try {
                // 1. Lista powiadomień
                val listResponse = apiService.getNotifications(tenantId, userId)
                if (listResponse.isSuccessful) {
                    _notifications.value = listResponse.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Nie udało się pobrać powiadomień (${listResponse.code()})."
                }

                // 2. Licznik nieprzeczytanych
                val countResponse = apiService.getUnreadCount(tenantId, userId)
                if (countResponse.isSuccessful) {
                    _unreadCount.value = countResponse.body() ?: 0
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd połączenia z serwerem."
                Log.e(TAG, "fetchNotifications error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─── Oznacz jedno powiadomienie jako przeczytane ──────────────────────────

    fun markRead(notificationId: Long) {
        val (tenantId, userId) = getSessionData()
        if (tenantId == -1L || userId == -1L) return

        viewModelScope.launch {
            try {
                val response = apiService.markAsRead(tenantId, userId, notificationId)
                if (response.isSuccessful) {
                    // Aktualizacja lokalna — natychmiastowa reakcja UI
                    _notifications.value = _notifications.value.map {
                        if (it.id == notificationId) it.copy(read = true) else it
                    }
                    _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
                }
            } catch (e: Exception) {
                Log.e(TAG, "markRead error", e)
            }
        }
    }

    // ─── Oznacz wszystkie powiadomienia jako przeczytane ─────────────────────

    fun markAllAsRead() {
        val (tenantId, userId) = getSessionData()
        if (tenantId == -1L || userId == -1L) return

        viewModelScope.launch {
            try {
                val response = apiService.markAllNotificationsAsRead(tenantId, userId)
                if (response.isSuccessful) {
                    // Aktualizacja lokalna
                    _notifications.value = _notifications.value.map { it.copy(read = true) }
                    _unreadCount.value = 0
                }
            } catch (e: Exception) {
                Log.e(TAG, "markAllAsRead error", e)
            }
        }
    }
}