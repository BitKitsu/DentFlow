package com.example.dentflow_android.data.ViewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dentflow_android.data.remote.*
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
    private val prefs: SharedPreferences // Wstrzykujemy prefs dla dynamicznych ID
) : ViewModel() {

    private val _slots = MutableStateFlow<List<ScheduleSlotDTO>>(emptyList())
    val slots: StateFlow<List<ScheduleSlotDTO>> = _slots.asStateFlow()

    private val _blockers = MutableStateFlow<List<ScheduleBlockerDTO>>(emptyList())
    val blockers: StateFlow<List<ScheduleBlockerDTO>> = _blockers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val TAG = "SCHEDULE_VM_DEBUG"

    // --- POBIERANIE DANYCH SESJI ---
    private val currentTenantId: Long get() = prefs.getLong("tenant_id", -1L)
    private val currentUserId: Long get() = prefs.getLong("user_id", -1L)
    private val currentUserRole: String get() = prefs.getString("user_role", "STAFF") ?: "STAFF"

    private fun hasValidSession(): Boolean {
        if (currentTenantId == -1L || currentUserId == -1L) {
            Log.e(TAG, "BŁĄD: Brak aktywnej sesji (tenantId: $currentTenantId, userId: $currentUserId)")
            return false
        }
        return true
    }

    // --- ŁADOWANIE DANYCH ---
    fun loadSchedule() {
        if (!hasValidSession()) return

        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Ładowanie grafiku dla tenantId: $currentTenantId, role: $currentUserRole")
            try {
                val now = OffsetDateTime.now()
                val fromStr = now.minusMonths(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                val toStr = now.plusMonths(3).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                // Pobieranie slotów (terminów)
                val slotsRes = apiService.getSlots(currentTenantId, fromStr, toStr)
                if (slotsRes.isSuccessful) {
                    val allSlots = slotsRes.body() ?: emptyList()
                    // Jeśli to lekarz, filtrujemy tylko jego sloty (o ile backend nie robi tego sam)
                    _slots.value = if (currentUserRole == "DENTIST") {
                        allSlots.filter { it.staffId == currentUserId }
                    } else allSlots
                    Log.d(TAG, "Pobrano ${_slots.value.size} slotów")
                } else {
                    Log.e(TAG, "Błąd slotów: ${slotsRes.code()}")
                }

                // Pobieranie blokerów (przerw/urlopów)
                val blockersRes = apiService.getBlockers(currentTenantId)
                if (blockersRes.isSuccessful) {
                    val allBlockers = blockersRes.body() ?: emptyList()
                    _blockers.value = if (currentUserRole == "DENTIST") {
                        allBlockers.filter { it.staffId == currentUserId }
                    } else allBlockers
                    Log.d(TAG, "Pobrano ${_blockers.value.size} blokerów")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek podczas ładowania grafiku: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- OPERACJE NA SLOTACH ---
    fun addSlot(slot: ScheduleSlotDTO) {
        if (!hasValidSession()) return

        viewModelScope.launch {
            Log.d(TAG, "Dodawanie slotu: Staff: ${slot.staffId}, Start: ${slot.startAt}")
            val request = CreateSlotRequest(
                staffId = slot.staffId,
                locationId = slot.locationId,
                roomId = slot.roomId,
                startAt = slot.startAt,
                endAt = slot.endAt
            )
            try {
                val res = apiService.createSlot(currentTenantId, request)
                if (res.isSuccessful) {
                    Log.d(TAG, "Slot dodany pomyślnie")
                    loadSchedule()
                } else {
                    Log.e(TAG, "Błąd dodawania slotu: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek addSlot: ${e.message}")
            }
        }
    }

    fun updateSlot(slotId: Long, slot: ScheduleSlotDTO) {
        if (!hasValidSession()) return

        viewModelScope.launch {
            Log.d(TAG, "Aktualizacja slotu ID: $slotId")
            val request = UpdateSlotRequest(
                locationId = slot.locationId,
                roomId = slot.roomId,
                startAt = slot.startAt,
                endAt = slot.endAt
            )
            try {
                if (apiService.updateSlot(currentTenantId, slotId, request).isSuccessful) {
                    Log.d(TAG, "Slot zaktualizowany")
                    loadSchedule()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek updateSlot: ${e.message}")
            }
        }
    }

    fun deleteSlot(slotId: Long) {
        if (!hasValidSession()) return

        viewModelScope.launch {
            Log.d(TAG, "Usuwanie slotu ID: $slotId")
            try {
                if (apiService.deleteSlot(currentTenantId, slotId).isSuccessful) {
                    Log.d(TAG, "Slot usunięty")
                    loadSchedule()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek deleteSlot: ${e.message}")
            }
        }
    }

    // --- OPERACJE NA BLOKERACH (URLOPY/PRZERWY) ---
    fun addBlocker(blocker: ScheduleBlockerDTO) {
        if (!hasValidSession()) return

        viewModelScope.launch {
            Log.d(TAG, "Dodawanie blokady: ${blocker.reason}")
            val request = CreateBlockerRequest(
                staffId = blocker.staffId,
                roomId = blocker.roomId,
                startAt = blocker.startAt,
                endAt = blocker.endAt,
                reason = blocker.reason
            )
            try {
                if (apiService.createBlocker(currentTenantId, request).isSuccessful) {
                    Log.d(TAG, "Blokada dodana")
                    loadSchedule()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek addBlocker: ${e.message}")
            }
        }
    }

    fun deleteBlocker(blockerId: Long) {
        if (!hasValidSession()) return

        viewModelScope.launch {
            Log.d(TAG, "Usuwanie blokady ID: $blockerId")
            try {
                if (apiService.deleteBlocker(currentTenantId, blockerId).isSuccessful) {
                    Log.d(TAG, "Blokada usunięta")
                    loadSchedule()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wyjątek deleteBlocker: ${e.message}")
            }
        }
    }
}