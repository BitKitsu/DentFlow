package pl.edu.ur.dentflow.core.file.api;

/**
 * DTO contains metadata of the uploaded fle returned after upload
 * Field publicUrl contains public link for direct download from S3
 */
public record FileUploadResponse(
        Long id,
        String originalName,
        String storagePath,
        String publicUrl,
        String contentType,
        Long sizeBytes,
        String createdAt
) {}
