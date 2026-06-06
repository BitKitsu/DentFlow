package pl.edu.ur.dentflow.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/tenant")
    suspend fun assignTenant(@Body request: AssignTenantRequest): Response<AuthResponse>

    @PUT("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<AuthResponse>

    @DELETE("auth/account")
    suspend fun deleteAccount(): Response<Unit>

    @GET("auth/check-email")
    suspend fun checkEmailExists(@Query("email") email: String): Response<Long>

    @GET("auth/user-by-email")
    suspend fun getUserByEmail(@Query("email") email: String): Response<AuthResponse>

    @POST("auth/assign-role")
    suspend fun assignRole(@Body request: AssignRoleRequest): Response<AuthResponse>
}

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
