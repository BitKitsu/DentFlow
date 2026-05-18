package com.dentflow.core.file.application;

import com.dentflow.core.file.api.FileUploadResponse;
import com.dentflow.core.file.domain.FileMetadata;
import com.dentflow.core.file.infrastructure.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Serwis obsługujący upload/download plików przez Supabase Storage REST API.
 * SCRUM-64
 *
 * Supabase Storage API:
 *   Upload: POST /storage/v1/object/{bucket}/{path}
 *   Download: GET  /storage/v1/object/{bucket}/{path}
 *   Delete:  DELETE /storage/v1/object/{bucket} (body: {"prefixes": [path]})
 */
@Service
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final RestTemplate restTemplate;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String supabaseServiceKey;

    @Value("${supabase.bucket:dentflow-files}")
    private String bucket;

    public FileService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Przesyła plik do Supabase Storage i zapisuje metadane w bazie.
     */
    public FileUploadResponse uploadFile(Long tenantId, Long uploadedByUserId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plik jest pusty");
        }

        String storagePath = tenantId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + storagePath;

        try {
            HttpHeaders headers = buildAuthHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE));

            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Błąd przesyłania pliku do Supabase: " + e.getMessage());
        }

        FileMetadata metadata = FileMetadata.builder()
                .tenantId(tenantId)
                .originalName(file.getOriginalFilename())
                .storagePath(storagePath)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .uploadedBy(uploadedByUserId)
                .build();

        FileMetadata saved = fileMetadataRepository.save(metadata);
        return toResponse(saved);
    }

    /**
     * Pobiera plik z Supabase Storage jako bajty.
     */
    public byte[] downloadFile(Long tenantId, Long fileId) {
        FileMetadata metadata = findOrThrow(tenantId, fileId);

        String downloadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + metadata.getStoragePath();

        try {
            HttpEntity<Void> entity = new HttpEntity<>(buildAuthHeaders());
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    downloadUrl, HttpMethod.GET, entity, byte[].class);

            if (response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plik nie istnieje w storage");
            }
            return response.getBody();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Błąd pobierania pliku z Supabase: " + e.getMessage());
        }
    }

    /**
     * Zwraca listę metadanych plików dla danego tenanta.
     */
    public List<FileUploadResponse> listFiles(Long tenantId) {
        return fileMetadataRepository.findByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Usuwa plik z Supabase Storage i jego metadane z bazy.
     */
    public void deleteFile(Long tenantId, Long fileId) {
        FileMetadata metadata = findOrThrow(tenantId, fileId);

        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket;
        String body = "{\"prefixes\":[\"" + metadata.getStoragePath() + "\"]}";

        try {
            HttpHeaders headers = buildAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, String.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Błąd usuwania pliku z Supabase: " + e.getMessage());
        }

        fileMetadataRepository.delete(metadata);
    }

    public FileUploadResponse getFileMetadata(Long tenantId, Long fileId) {
        return toResponse(findOrThrow(tenantId, fileId));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private FileMetadata findOrThrow(Long tenantId, Long fileId) {
        return fileMetadataRepository.findByIdAndTenantId(fileId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Plik nie istnieje"));
    }

    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseServiceKey);
        headers.set("apikey", supabaseServiceKey);
        return headers;
    }

    private FileUploadResponse toResponse(FileMetadata m) {
        return new FileUploadResponse(
                m.getId(),
                m.getOriginalName(),
                m.getStoragePath(),
                m.getContentType(),
                m.getSizeBytes(),
                m.getCreatedAt() != null ? m.getCreatedAt().toString() : null
        );
    }
}
