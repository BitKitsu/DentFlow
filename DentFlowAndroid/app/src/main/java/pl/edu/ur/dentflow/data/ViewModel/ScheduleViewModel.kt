package pl.edu.ur.dentflow.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.edu.ur.dentflow.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val apiService: ApiService,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _blockers = MutableStateFlow<List<ScheduleBlockerDTO>>(emptyList())
    val blockers: StateFlow<List<ScheduleBlockerDTO>> = _blockers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val TAG = "SCHEDULE_VM_DEBUG"

    private val currentTenantId: Long get() = prefs.getLong("tenant_id", -1L)
    private val currentUserId: Long get() = prefs.getLong("user_id", -1L)

    private fun hasValidSession(): Boolean {
        if (currentTenantId == -1L || currentUserId == -1L) {
            Log.e(TAG, "Error: No active session (tenantId: $currentTenantId, userId: $currentUserId)")
            return false
        }
        return true
    }

    // --- Data Loading ---
    fun loadSchedule() {
        if (!hasValidSession()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val blockersRes = apiService.getBlockers(currentTenantId)
                if (blockersRes.isSuccessful) {
                    _blockers.value = blockersRes.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading schedule: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Blocker Operations (Breaks/Leave) ---
    fun addBlocker(blocker: ScheduleBlockerDTO) {
        if (!hasValidSession()) return

        viewModelScope.launch {
            val effectiveStaffId = if (blocker.staffId > 0) blocker.staffId else null
            val request = CreateBlockerRequest(
                staffId = effectiveStaffId,
                roomId = if (blocker.roomId != null && blocker.roomId > 0) blocker.roomId else null,
                startAt = blocker.startAt,
                endAt = blocker.endAt,
                reason = blocker.reason
            )
            try {
                if (apiService.createBlocker(currentTenantId, request).isSuccessful) {
                    loadSchedule()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception addBlocker: ${e.message}")
            }
        }
    }

    fun deleteBlocker(blockerId: Long) {
        if (!hasValidSession()) return

        viewModelScope.launch {
            try {
                if (apiService.deleteBlocker(currentTenantId, blockerId).isSuccessful) {
                    loadSchedule()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception deleteBlocker: ${e.message}")
            }
        }
    }
}
