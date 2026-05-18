package com.dentflow.core.file.api;

/**
 * DTO zawierające metadane przesłanego pliku zwracane po upload.
 */
public record FileUploadResponse(
        Long id,
        String originalName,
        String storagePath,
        String contentType,
        Long sizeBytes,
        String createdAt
) {}
