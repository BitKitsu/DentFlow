package pl.edu.ur.dentflow.data.remote

import com.google.gson.annotations.SerializedName

// --- TENANTS & REGISTRATION ---

data class TenantResponse(
    val id: Long,
    val name: String,
    val status: String? = null,
    val logoUrl: String? = null,
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
    val logoUrl: String? = null,
    val locationName: String? = null,
    val addressStreet: String? = null,
    val addressCity: String? = null,
    val addressZip: String? = null,
    val addressCountry: String? = null
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
    val notes: String? = null,
    val dateOfBirth: String? = null,
    val pesel: String? = null,
    val gender: String? = null,
    val addressStreet: String? = null,
    val addressCity: String? = null,
    val addressZip: String? = null,
    val addressCountry: String? = null,
    val avatarUrl: String? = null
)

data class CreatePatientRequest(
    val userId: Long? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val notes: String = "",
    val dateOfBirth: String? = null,
    val pesel: String? = null,
    val gender: String? = null,
    val addressStreet: String? = null,
    val addressCity: String? = null,
    val addressZip: String? = null,
    val addressCountry: String? = null,
    val avatarUrl: String? = null
)

data class UpdatePatientRequest(
    val userId: Long? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val notes: String = "",
    val dateOfBirth: String? = null,
    val pesel: String? = null,
    val gender: String? = null,
    val addressStreet: String? = null,
    val addressCity: String? = null,
    val addressZip: String? = null,
    val addressCountry: String? = null,
    val avatarUrl: String? = null
)

// --- STAFF ---

data class StaffMemberResponse(
    val id: Long,
    val tenantId: Long,
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val profession: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val workingHoursStart: String? = null,
    val workingHoursEnd: String? = null
)

data class CreateStaffMemberRequest(
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val profession: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val email: String? = null
)

data class SyncFromUserRequest(
    val userId: Long,
    val firstName: String?,
    val lastName: String?,
    val avatarUrl: String?,
    val phone: String?,
    val email: String?
)

data class UpdateStaffMemberRequest(
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val profession: String,
    val bio: String? = null
)

// --- APPOINTMENTS (Wizyty) ---

data class AppointmentResponse(
    val id: Long,
    val tenantId: Long,
    val locationId: Long,
    val roomId: Long? = null,
    val dentistStaffId: Long,
    val patientId: Long? = null,
    val serviceItemId: Long? = null,
    val startAt: String,
    val endAt: String,
    val status: String,
    val notes: String? = null,
    val createdByUserId: Long? = null
)

data class CreateAppointmentRequest(
    val locationId: Long,
    val roomId: Long? = null,
    val dentistStaffId: Long,
    val patientId: Long? = null,
    val serviceItemId: Long? = null,
    val startAt: String,
    val endAt: String,
    val createdByUserId: Long,
    val notes: String = ""
)

// --- SCHEDULING (Blockers) ---

data class ScheduleBlockerDTO(
    val id: Long,
    val tenantId: Long,
    val staffId: Long,
    val roomId: Long? = null,
    val startAt: String,
    val endAt: String,
    val reason: String
)

data class CreateBlockerRequest(
    val staffId: Long?,
    val roomId: Long?,
    val startAt: String,
    val endAt: String,
    val reason: String
)

// --- OTHERS ---

data class RoomResponse(
    val id: Long,
    val tenantId: Long,
    val locationId: Long,
    val name: String,
    val assignedStaffIds: List<Long> = emptyList()
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
    val serviceItemId: Long? = null,
    val roomId: Long? = null,
    val notes: String? = null
)

// --- WORKING HOURS ---
data class StaffWorkingHoursDTO(
    val id: Long,
    val staffMemberId: Long,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String
)

data class WorkingHoursEntry(
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String
)

data class UpdateWorkingHoursRequest(
    val schedule: List<WorkingHoursEntry>
)