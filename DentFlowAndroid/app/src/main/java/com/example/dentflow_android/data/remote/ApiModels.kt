package com.example.dentflow_android.data.remote

import com.google.gson.annotations.SerializedName

// --- TENANTS & REGISTRATION ---

data class TenantResponse(
    val id: Long,
    val name: String,
    val status: String? = null,
    val locations: List<LocationResponse>? = emptyList()
)
data class ServiceCatalogRequest(
    val name: String,
    val durationMinutes: Int,
    val priceCents: Int,
    val active: Boolean = true
)

data class AddLocationRequest(
    val name: String,
    val addressStreet: String,
    val addressCity: String,
    val addressZip: String,
    val addressCountry: String = "Polska"
)

data class TenantRequest(
    val name: String,
    val location: LocationRequest
)

data class RegisterTenantRequest(
    val name: String,
    val locationName: String,
    val addressStreet: String,
    val addressCity: String,
    val addressZip: String,
    val addressCountry: String
)

data class AssignTenantRequest(
    val tenantId: Long
)

data class LocationRequest(
    val name: String,
    val addressStreet: String,
    val addressCity: String,
    val addressZip: String,
    val addressCountry: String = "Polska",
    val phone: String = ""
)

data class LocationResponse(
    val id: Long,
    val tenantId: Long,
    val name: String,
    val addressStreet: String,
    val addressCity: String,
    val addressZip: String,
    val addressCountry: String,
    val phone: String? = null
)

// --- PATIENTS ---

data class PatientResponse(
    val id: Long,
    val tenantId: Long,
    val userId: Long? = null,
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val phone: String,
    val notes: String? = null
)

data class CreatePatientRequest(
    val userId: Long? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val notes: String = ""
)

data class UpdatePatientRequest(
    val userId: Long? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val notes: String = ""
)

// --- STAFF ---

data class StaffMemberResponse(
    val id: Long,
    val tenantId: Long,
    val userId: Long,
    val displayName: String,
    val profession: String
)

data class CreateStaffMemberRequest(
    val userId: Long,
    val displayName: String,
    val profession: String
)

data class UpdateStaffMemberRequest(
    val userId: Long,
    val displayName: String,
    val profession: String
)

// --- APPOINTMENTS (Wizyty) ---

data class AppointmentResponse(
    val id: Long,
    val tenantId: Long,
    val locationId: Long,
    val roomId: Long,
    val dentistStaffId: Long,
    val patientId: Long,
    val serviceItemId: Long,
    val startAt: String, // Zmienione z startTime zgodnie z API
    val endAt: String,   // Zmienione z endTime zgodnie z API
    val status: String,
    val notes: String? = null,
    val createdByUserId: Long? = null
)

data class CreateAppointmentRequest(
    val locationId: Long,
    val roomId: Long,
    val dentistStaffId: Long,
    val patientId: Long,
    val serviceItemId: Long,
    val startAt: String,
    val endAt: String,
    val createdByUserId: Long,
    val notes: String = ""
)

// --- SCHEDULING (Sloty i Blokady) ---

data class ScheduleSlotDTO(
    val id: Long,
    val tenantId: Long,
    val staffId: Long,
    val locationId: Long,
    val roomId: Long,
    val startAt: String,
    val endAt: String
)

data class CreateSlotRequest(
    val staffId: Long,
    val locationId: Long,
    val roomId: Long,
    val startAt: String,
    val endAt: String
)

data class ScheduleBlockerDTO(
    val id: Long,
    val tenantId: Long,
    val staffId: Long,
    val roomId: Long,
    val startAt: String,
    val endAt: String,
    val reason: String
)

data class CreateBlockerRequest(
    val staffId: Long,
    val roomId: Long,
    val startAt: String,
    val endAt: String,
    val reason: String
)

data class UpdateSlotRequest(
    val locationId: Long,
    val roomId: Long,
    val startAt: String,
    val endAt: String
)

// --- OTHERS ---

data class RoomResponse(
    val id: Long,
    val tenantId: Long,
    val locationId: Long,
    val name: String
)

data class ServiceCatalogItemDTO(
    val id: Long,
    val tenantId: Long,
    val name: String,
    val durationMinutes: Int,
    val priceCents: Long,
    val active: Boolean
)

data class NotificationDTO(
    val id: Long,
    val tenantId: Long,
    val userId: Long,
    val type: String,
    val message: String,
    val read: Boolean,
    val createdAt: String
)

data class CreateNotificationRequest(
    val userId: Long,
    val type: String,
    val message: String
)

data class UpdateAppointmentRequest(
    val startAt: String,
    val endAt: String,
    val serviceItemId: Long,
    val roomId: Long,
    val notes: String
)