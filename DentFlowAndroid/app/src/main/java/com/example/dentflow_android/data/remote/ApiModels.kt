package com.example.dentflow_android.data.remote

import com.google.gson.annotations.SerializedName

// --- GABINET (TENANT) ---
data class TenantResponse(
    val id: Long,
    val name: String,
    val status: String? = null,
    val locations: List<LocationResponse>? = emptyList()
)

data class LocationResponse(
    val id: Long,
    val tenantId: Long,
    val name: String,
    // Korzystamy z camelCase dokładnie tak, jak zwraca Twój backend
    val addressStreet: String,
    val addressCity: String,
    val addressZip: String,
    val addressCountry: String
)

// --- PACJENCI ---
data class PatientResponse(
    val id: Long,
    val tenantId: Long,
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val phone: String,
    val notes: String? = null
)

// --- PERSONEL ---
data class StaffMemberResponse(
    val id: Long,
    val tenantId: Long,
    val userId: Long,
    val displayName: String,
    val profession: String
)

// --- WIZYTY ---
data class VisitResponse(
    val id: Long,
    val tenantId: Long,
    val patientId: Long,
    val staffId: Long? = null,
    val startTime: String,
    val endTime: String? = null,
    val description: String? = null,
    val status: String
)

// --- REQUESTS (Wysyłanie danych do bazy) ---
data class LocationRequest(
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

data class CreatePatientRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val notes: String = ""
)
