package com.example.dentflow_android.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

data class FileUploadResponse(
    val id: Long,
    val originalName: String,
    val storagePath: String,
    val publicUrl: String,
    val contentType: String?,
    val sizeBytes: Long,
    val createdAt: String?
)

interface FileApiService {
    @Multipart
    @POST("tenants/{tenantId}/files")
    suspend fun uploadFile(
        @Path("tenantId") tenantId: Long,
        @Query("uploadedByUserId") uploadedByUserId: Long = 0,
        @Part file: MultipartBody.Part
    ): Response<FileUploadResponse>
}
