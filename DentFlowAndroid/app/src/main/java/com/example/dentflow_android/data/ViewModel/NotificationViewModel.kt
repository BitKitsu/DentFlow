package com.example.dentflow_android.data.ViewModel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.ApiService
import com.example.dentflow_android.data.remote.NotificationDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationDTO>>(emptyList())
    val notifications: StateFlow<List<NotificationDTO>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private fun getSessionData(): Pair<Long, Long> {
        val tenantId = sharedPrefs.getLong("tenant_id", -1L)
        val userId = sharedPrefs.getLong("user_id", -1L)
        return Pair(tenantId, userId)
    }

    fun fetchNotifications() {
        val (tenantId, userId) = getSessionData()
        if (tenantId == -1L || userId == -1L) return

        viewModelScope.launch {
            try {
                // 1. Pobieranie listy
                val response = apiService.getNotifications(tenantId, userId)
                if (response.isSuccessful) {
                    _notifications.value = response.body() ?: emptyList()
                }

                // 2. Pobieranie licznika nieprzeczytanych
                val countResponse = apiService.getUnreadCount(tenantId, userId)
                if (countResponse.isSuccessful) {
                    _unreadCount.value = countResponse.body() ?: 0
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markAllAsRead() {
        val (tenantId, userId) = getSessionData()
        if (tenantId == -1L || userId == -1L) return

        viewModelScope.launch {
            try {
                // UŻYTO TWOJEJ NAZWY: markAllNotificationsAsRead
                val response = apiService.markAllNotificationsAsRead(tenantId, userId)
                if (response.isSuccessful) {
                    fetchNotifications() // Odświeżamy dane
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markRead(notificationId: Long) {
        val (tenantId, userId) = getSessionData()
        if (tenantId == -1L || userId == -1L) return

        viewModelScope.launch {
            try {
                // UŻYTO TWOJEJ NAZWY: markAsRead
                val response = apiService.markAsRead(tenantId, userId, notificationId)
                if (response.isSuccessful) {
                    // Aktualizujemy lokalnie, żeby UI zareagował natychmiast
                    _notifications.value = _notifications.value.map {
                        if (it.id == notificationId) it.copy(read = true) else it
                    }
                    _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}