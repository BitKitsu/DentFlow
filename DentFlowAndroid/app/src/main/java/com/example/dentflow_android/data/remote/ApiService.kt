package com.example.dentflow_android.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- GABINET (TENANT) ---
    @POST("tenants")
    suspend fun createTenant(
        @Body request: TenantRequest
    ): Response<TenantResponse>

    @GET("tenants/{tenantId}")
    suspend fun getTenantDetails(
        @Path("tenantId") tenantId: Long
    ): Response<TenantResponse>

    @PUT("tenants/{tenantId}")
    suspend fun updateTenant(
        @Path("tenantId") tenantId: Long,
        @Body request: TenantRequest
    ): Response<TenantResponse>

    // --- PACJENCI ---
    @GET("tenants/{tenantId}/patients")
    suspend fun getPatients(
        @Path("tenantId") tenantId: Long,
        @Query("search") search: String? = null
    ): Response<List<PatientResponse>>

    @GET("tenants/{tenantId}/patients/{patientId}")
    suspend fun getPatientById(
        @Path("tenantId") tenantId: Long,
        @Path("patientId") patientId: Long
    ): Response<PatientResponse>

    @POST("tenants/{tenantId}/patients")
    suspend fun createPatient(
        @Path("tenantId") tenantId: Long,
        @Body request: CreatePatientRequest
    ): Response<PatientResponse>

    @PUT("tenants/{tenantId}/patients/{patientId}")
    suspend fun updatePatient(
        @Path("tenantId") tenantId: Long,
        @Path("patientId") patientId: Long,
        @Body request: CreatePatientRequest
    ): Response<PatientResponse>

    @DELETE("tenants/{tenantId}/patients/{patientId}")
    suspend fun deletePatient(
        @Path("tenantId") tenantId: Long,
        @Path("patientId") patientId: Long
    ): Response<Unit>

    // --- PERSONEL (STAFF) ---
    @GET("tenants/{tenantId}/staff")
    suspend fun getStaff(
        @Path("tenantId") tenantId: Long
    ): Response<List<StaffMemberResponse>>

    // --- WIZYTY ---
    @GET("tenants/{tenantId}/visits")
    suspend fun getVisits(
        @Path("tenantId") tenantId: Long
    ): Response<List<VisitResponse>>
}
