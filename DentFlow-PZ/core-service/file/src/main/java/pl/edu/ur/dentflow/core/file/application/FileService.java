package pl.edu.ur.dentflow.core.file.application;

import pl.edu.ur.dentflow.core.file.api.FileUploadResponse;
import pl.edu.ur.dentflow.core.file.domain.FileMetadata;
import pl.edu.ur.dentflow.core.file.infrastructure.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service that manages file upload/download via S3 bucker
 *
 * POST /tenants/{tenantId}/files – file upload (max 10MB)
 * GET /tenants/{tenantId}/files - file list
 * GET /tenants/{tenantId}/files/{fileId} - file metadata
 * GET /tenants/{tenantId}/files/{fileId}/download – download file
 * DELETE /tenants/{tenantId}/files/{fileId} - remove file
 */
@Service
public class FileService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private final FileMetadataRepository fileMetadataRepository;
    private final S3Client s3Client;

    @Value("${aws.bucket-name}")
    private String bucket;

    @Value("${aws.endpoint-url}")
    private String endpointUrl;

    @Value("${RAILWAY_PUBLIC_DOMAIN:localhost:8080}")
    private String appPublicDomain;

    public FileService(FileMetadataRepository fileMetadataRepository, S3Client s3Client) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.s3Client = s3Client;
    }

    /**
     * Uploads file to S3 and saves metadata to database
     * Returns file public url
     */
    public FileUploadResponse uploadFile(Long tenantId, Long uploadedByUserId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plik jest pusty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Plik przekracza maksymalny rozmiar 10 MB");
        }

        String extension = "";
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        Long actualTenantId = (tenantId != null && tenantId > 0) ? tenantId : null;
        String prefix = actualTenantId != null ? actualTenantId.toString() : "global";
        String key = prefix + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType() != null
                            ? file.getContentType() : "application/octet-stream")
                    .contentLength(file.getSize())
                    .acl(ObjectCannedACL.PUBLIC_READ) // public access
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Nie można odczytać danych pliku: " + e.getMessage());
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Błąd przesyłania pliku do S3: " + e.getMessage());
        }

        // Public_url is set after save
        FileMetadata metadata = FileMetadata.builder()
                .tenantId(actualTenantId)
                .originalName(originalFilename)
                .storagePath(key)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .uploadedBy(uploadedByUserId)
                .build();

        FileMetadata saved = fileMetadataRepository.save(metadata);
        String protocol = appPublicDomain.contains("localhost") ? "http://" : "https://";
        saved.setPublicUrl(protocol + appPublicDomain + "/public/files/" + saved.getId());
        saved = fileMetadataRepository.save(saved);

        return toResponse(saved);
    }

    /**
     * Downloads file from S3 as bytes
     */
    public byte[] downloadFile(Long tenantId, Long fileId) {
        FileMetadata metadata = findOrThrow(tenantId, fileId);
        return downloadFileBytes(metadata);
    }

    public byte[] downloadFileBytes(FileMetadata metadata) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(metadata.getStoragePath())
                    .build();

            return s3Client.getObjectAsBytes(getRequest).asByteArray();

        } catch (NoSuchKeyException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plik nie istnieje w storage");
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Błąd pobierania pliku z S3: " + e.getMessage());
        }
    }

    public List<FileUploadResponse> listFiles(Long tenantId) {
        Long actualTenantId = (tenantId != null && tenantId > 0) ? tenantId : null;
        List<FileMetadata> files = actualTenantId == null
                ? fileMetadataRepository.findByTenantIdIsNull()
                : fileMetadataRepository.findByTenantId(actualTenantId);

        return files.stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteFile(Long tenantId, Long fileId) {
        FileMetadata metadata = findOrThrow(tenantId, fileId);

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(metadata.getStoragePath())
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Błąd usuwania pliku z S3: " + e.getMessage());
        }

        fileMetadataRepository.delete(metadata);
    }

    public FileUploadResponse getFileMetadata(Long tenantId, Long fileId) {
        return toResponse(findOrThrow(tenantId, fileId));
    }

    public FileMetadata getFileMetadataEntity(Long fileId) {
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plik nie istnieje"));
    }

    // helpers

    private FileMetadata findOrThrow(Long tenantId, Long fileId) {
        Long actualTenantId = (tenantId != null && tenantId > 0) ? tenantId : null;
        if (actualTenantId == null) {
            return fileMetadataRepository.findById(fileId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plik nie istnieje"));
        }
        return fileMetadataRepository.findByIdAndTenantId(fileId, actualTenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Plik nie istnieje"));
    }

    private FileUploadResponse toResponse(FileMetadata m) {
        return new FileUploadResponse(
                m.getId(),
                m.getOriginalName(),
                m.getStoragePath(),
                m.getPublicUrl(),
                m.getContentType(),
                m.getSizeBytes(),
                m.getCreatedAt() != null ? m.getCreatedAt().toString() : null
        );
    }
}
