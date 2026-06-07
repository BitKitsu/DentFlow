package pl.edu.ur.dentflow.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- TENANTS (KLINIKI) ---
    @GET("tenants")
    suspend fun getAllTenants(): Response<List<TenantResponse>>

    @POST("tenants/register")
    suspend fun registerTenant(
        @Body request: RegisterTenantRequest
    ): Response<TenantResponse>

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

    @DELETE("tenants/{tenantId}")
    suspend fun deleteTenant(
        @Path("tenantId") tenantId: Long
    ): Response<Unit>

    // --- ROOMS (GABINETY) ---
    @GET("tenants/{tenantId}/rooms")
    suspend fun getRooms(
        @Path("tenantId") tenantId: Long
    ): Response<List<RoomResponse>>

    @POST("tenants/{tenantId}/rooms")
    suspend fun createRoom(
        @Path("tenantId") tenantId: Long,
        @Query("name") name: String,
        @Query("locationId") locationId: Long
    ): Response<RoomResponse>

    @PUT("tenants/{tenantId}/rooms/{roomId}")
    suspend fun updateRoom(
        @Path("tenantId") tenantId: Long,
        @Path("roomId") roomId: Long,
        @Query("name") name: String,
        @Query("locationId") locationId: Long
    ): Response<RoomResponse>

    @DELETE("tenants/{tenantId}/rooms/{roomId}")
    suspend fun deleteRoom(
        @Path("tenantId") tenantId: Long,
        @Path("roomId") roomId: Long
    ): Response<Unit>

    @POST("tenants/{tenantId}/rooms/{roomId}/staff/{staffId}")
    suspend fun assignStaffToRoom(
        @Path("tenantId") tenantId: Long,
        @Path("roomId") roomId: Long,
        @Path("staffId") staffId: Long
    ): Response<Unit>

    @DELETE("tenants/{tenantId}/rooms/{roomId}/staff/{staffId}")
    suspend fun removeStaffFromRoom(
        @Path("tenantId") tenantId: Long,
        @Path("roomId") roomId: Long,
        @Path("staffId") staffId: Long
    ): Response<Unit>

    // --- PATIENTS (PACJENCI) ---
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

    @POST("tenants/{tenantId}/patients/ensure")
    suspend fun ensurePatient(
        @Path("tenantId") tenantId: Long,
        @Query("userId") userId: Long,
        @Query("firstName") firstName: String = "",
        @Query("lastName") lastName: String = "",
        @Query("email") email: String = ""
    ): Response<PatientResponse>

    @PUT("tenants/{tenantId}/patients/{patientId}")
    suspend fun updatePatient(
        @Path("tenantId") tenantId: Long,
        @Path("patientId") patientId: Long,
        @Body request: UpdatePatientRequest
    ): Response<PatientResponse>

    @DELETE("tenants/{tenantId}/patients/{patientId}")
    suspend fun deletePatient(
        @Path("tenantId") tenantId: Long,
        @Path("patientId") patientId: Long
    ): Response<Unit>

    // --- STAFF (PERSONEL) ---
    @GET("tenants/{tenantId}/staff")
    suspend fun getStaffMembers(
        @Path("tenantId") tenantId: Long
    ): Response<List<StaffMemberResponse>>

    @GET("tenants/staff/all")
    suspend fun getAllStaffMembers(): Response<List<StaffMemberResponse>>

    @POST("tenants/{tenantId}/staff")
    suspend fun createStaffMember(
        @Path("tenantId") tenantId: Long,
        @Body request: CreateStaffMemberRequest
    ): Response<StaffMemberResponse>

    @PUT("tenants/{tenantId}/staff/{staffId}")
    suspend fun updateStaffMember(
        @Path("tenantId") tenantId: Long,
        @Path("staffId") staffId: Long,
        @Body request: UpdateStaffMemberRequest
    ): Response<StaffMemberResponse>

    @DELETE("tenants/{tenantId}/staff/{staffId}")
    suspend fun deleteStaffMember(
        @Path("tenantId") tenantId: Long,
        @Path("staffId") staffId: Long
    ): Response<Unit>

    @POST("tenants/{tenantId}/staff/sync-from-user")
    suspend fun syncStaffFromUser(
        @Path("tenantId") tenantId: Long,
        @Body request: SyncFromUserRequest
    ): Response<Unit>

    // --- WORKING HOURS (GODZINY PRACY) ---
    @GET("tenants/{tenantId}/staff/{staffId}/working-hours")
    suspend fun getWorkingHours(
        @Path("tenantId") tenantId: Long,
        @Path("staffId") staffId: Long
    ): Response<List<StaffWorkingHoursDTO>>

    @PUT("tenants/{tenantId}/staff/{staffId}/working-hours")
    suspend fun updateWorkingHours(
        @Path("tenantId") tenantId: Long,
        @Path("staffId") staffId: Long,
        @Body request: UpdateWorkingHoursRequest
    ): Response<Unit>

    // --- APPOINTMENTS (WIZYTY) ---
    @GET("tenants/{tenantId}/appointments")
    suspend fun getAppointments(
        @Path("tenantId") tenantId: Long,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<List<AppointmentResponse>>

    @GET("tenants/{tenantId}/appointments/my")
    suspend fun getMyAppointments(
        @Path("tenantId") tenantId: Long
    ): Response<List<AppointmentResponse>>

    @POST("tenants/{tenantId}/appointments")
    suspend fun createAppointment(
        @Path("tenantId") tenantId: Long,
        @Body request: CreateAppointmentRequest
    ): Response<AppointmentResponse>

    @GET("tenants/{tenantId}/appointments/{appointmentId}")
    suspend fun getAppointmentDetails(
        @Path("tenantId") tenantId: Long,
        @Path("appointmentId") appointmentId: Long
    ): Response<AppointmentResponse>

    @PUT("tenants/{tenantId}/appointments/{appointmentId}")
    suspend fun updateAppointment(
        @Path("tenantId") tenantId: Long,
        @Path("appointmentId") appointmentId: Long,
        @Body request: UpdateAppointmentRequest
    ): Response<AppointmentResponse>

    @POST("tenants/{tenantId}/appointments/{appointmentId}/confirm")
    suspend fun confirmAppointment(
        @Path("tenantId") tenantId: Long,
        @Path("appointmentId") appointmentId: Long
    ): Response<AppointmentResponse>

    @POST("tenants/{tenantId}/appointments/{appointmentId}/complete")
    suspend fun completeAppointment(
        @Path("tenantId") tenantId: Long,
        @Path("appointmentId") appointmentId: Long
    ): Response<AppointmentResponse>

    @POST("tenants/{tenantId}/appointments/{appointmentId}/cancel")
    suspend fun cancelAppointment(
        @Path("tenantId") tenantId: Long,
        @Path("appointmentId") appointmentId: Long
    ): Response<AppointmentResponse>

    // --- REPORTS (RAPORTY) ---
    @Streaming
    @GET("tenants/{tenantId}/reports/appointments")
    suspend fun getAppointmentReportPdf(
        @Path("tenantId") tenantId: Long,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("status") status: String? = null,
        @Query("dentistId") dentistId: Long? = null
    ): Response<okhttp3.ResponseBody>

    // --- CATALOG (USŁUGI) ---
    @GET("tenants/{tenantId}/catalog")
    suspend fun getServices(
        @Path("tenantId") tenantId: Long,
        @Query("activeOnly") activeOnly: Boolean = false
    ): Response<List<ServiceCatalogItemDTO>>

    @GET("tenants/catalog/all")
    suspend fun getAllActiveCatalog(): Response<List<ServiceCatalogItemDTO>>

    @POST("tenants/{tenantId}/catalog")
    suspend fun createService(
        @Path("tenantId") tenantId: Long,
        @Body request: ServiceCatalogRequest
    ): Response<ServiceCatalogItemDTO>

    @PUT("tenants/{tenantId}/catalog/{id}")
    suspend fun updateService(
        @Path("tenantId") tenantId: Long,
        @Path("id") id: Long,
        @Body request: ServiceCatalogRequest
    ): Response<ServiceCatalogItemDTO>

    @DELETE("tenants/{tenantId}/catalog/{id}")
    suspend fun deleteService(
        @Path("tenantId") tenantId: Long,
        @Path("id") id: Long
    ): Response<Unit>

    // --- SCHEDULE BLOCKERS (PRZERWY / URLOPY) ---
    @GET("tenants/{tenantId}/schedule/blockers")
    suspend fun getBlockers(
        @Path("tenantId") tenantId: Long
    ): Response<List<ScheduleBlockerDTO>>

    @POST("tenants/{tenantId}/schedule/blockers")
    suspend fun createBlocker(
        @Path("tenantId") tenantId: Long,
        @Body request: CreateBlockerRequest
    ): Response<ScheduleBlockerDTO>

    @DELETE("tenants/{tenantId}/schedule/blockers/{blockerId}")
    suspend fun deleteBlocker(
        @Path("tenantId") tenantId: Long,
        @Path("blockerId") blockerId: Long
    ): Response<Unit>

    // --- NOTIFICATIONS (POWIADOMIENIA) ---
    @GET("tenants/{tenantId}/users/{userId}/notifications")
    suspend fun getNotifications(
        @Path("tenantId") tenantId: Long,
        @Path("userId") userId: Long,
        @Query("unreadOnly") unreadOnly: Boolean = false
    ): Response<List<NotificationDTO>>

    @GET("tenants/{tenantId}/users/{userId}/notifications/unread-count")
    suspend fun getUnreadCount(
        @Path("tenantId") tenantId: Long,
        @Path("userId") userId: Long
    ): Response<Int>

    @POST("tenants/{tenantId}/users/{userId}/notifications/{notificationId}/read")
    suspend fun markAsRead(
        @Path("tenantId") tenantId: Long,
        @Path("userId") userId: Long,
        @Path("notificationId") notificationId: Long
    ): Response<NotificationDTO>

    @POST("tenants/{tenantId}/users/{userId}/notifications/read-all")
    suspend fun markAllNotificationsAsRead(
        @Path("tenantId") tenantId: Long,
        @Path("userId") userId: Long
    ): Response<Unit>

    // --- HISTORY (HISTORIA WIZYT PACJENTA) ---
    @GET("tenants/{tenantId}/patients/{patientId}/visits")
    suspend fun getPatientVisits(
        @Path("tenantId") tenantId: Long,
        @Path("patientId") patientId: Long,
        @Query("status") status: String? = null
    ): Response<List<AppointmentResponse>>

    @Streaming
    @GET("tenants/{tenantId}/patients/{patientId}/visits/pdf")
    suspend fun getPatientHistoryReportPdf(
        @Path("tenantId") tenantId: Long,
        @Path("patientId") patientId: Long,
        @Query("status") status: String? = null
    ): Response<ResponseBody>

    // --- REPORTS (RAPORTY) ---
    @Streaming
    @GET("tenants/{tenantId}/reports/room-occupancy")
    suspend fun getRoomOccupancyReport(
        @Path("tenantId") tenantId: Long,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<ResponseBody>

    @Streaming
    @GET("tenants/{tenantId}/reports/room-occupancy/{roomId}")
    suspend fun getSingleRoomOccupancyReport(
        @Path("tenantId") tenantId: Long,
        @Path("roomId") roomId: Long,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<ResponseBody>
}