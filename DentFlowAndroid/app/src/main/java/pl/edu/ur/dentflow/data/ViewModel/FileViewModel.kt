package pl.edu.ur.dentflow.data.ViewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.edu.ur.dentflow.data.remote.FileApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

private const val MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024 // 5 MB

@HiltViewModel
class FileViewModel @Inject constructor(
    private val fileApiService: FileApiService
) : ViewModel() {

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError

    /**
     * Uploads cropped image
     * @param context needed for reading Uri
     * @param tenantId clinic id
     * @param uri Uri of the cropped image
     * @param onSuccess returns public url of the uploaded fileu
     */
    fun uploadImage(
        context: Context,
        tenantId: Long,
        uri: Uri,
        onSuccess: (publicUrl: String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadError.value = null
            try {
                // Copy Uri to temp
                val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output -> input.copyTo(output) }
                }

                if (tempFile.length() > MAX_FILE_SIZE_BYTES) {
                    val msg = "Zdjęcie jest zbyt duże (max 5 MB)"
                    _uploadError.value = msg
                    onError(msg)
                    tempFile.delete()
                    return@launch
                }

                val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

                val response = fileApiService.uploadFile(tenantId, 0, part)
                tempFile.delete()

                if (response.isSuccessful) {
                    val publicUrl = response.body()?.publicUrl ?: ""
                    Log.d("FileViewModel", "Upload successful: $publicUrl")
                    onSuccess(publicUrl)
                } else {
                    val msg = "Błąd przesyłania pliku (${response.code()})"
                    _uploadError.value = msg
                    onError(msg)
                }
            } catch (e: Exception) {
                val msg = "Błąd połączenia: ${e.message}"
                Log.e("FileViewModel", msg, e)
                _uploadError.value = msg
                onError(msg)
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun clearError() { _uploadError.value = null }
}
