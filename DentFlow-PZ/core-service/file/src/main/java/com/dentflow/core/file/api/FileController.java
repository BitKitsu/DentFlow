package com.dentflow.core.file.api;

import com.dentflow.core.file.application.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Endpointy zarządzania plikami z integracją Supabase Storage.
 * SCRUM-64
 *
 * POST   /tenants/{tenantId}/files              – upload pliku
 * GET    /tenants/{tenantId}/files              – lista plików
 * GET    /tenants/{tenantId}/files/{fileId}     – metadane pliku
 * GET    /tenants/{tenantId}/files/{fileId}/download – pobierz plik
 * DELETE /tenants/{tenantId}/files/{fileId}     – usuń plik
 */
@RestController
@RequestMapping("/tenants/{tenantId}/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(
            @PathVariable Long tenantId,
            @RequestParam(defaultValue = "0") Long uploadedByUserId,
            @RequestParam("file") MultipartFile file) {

        FileUploadResponse response = fileService.uploadFile(tenantId, uploadedByUserId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FileUploadResponse>> listFiles(
            @PathVariable Long tenantId) {
        return ResponseEntity.ok(fileService.listFiles(tenantId));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileUploadResponse> getFileMetadata(
            @PathVariable Long tenantId,
            @PathVariable Long fileId) {
        return ResponseEntity.ok(fileService.getFileMetadata(tenantId, fileId));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long tenantId,
            @PathVariable Long fileId) {

        FileUploadResponse meta = fileService.getFileMetadata(tenantId, fileId);
        byte[] bytes = fileService.downloadFile(tenantId, fileId);

        String contentType = meta.contentType() != null
                ? meta.contentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + meta.originalName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long tenantId,
            @PathVariable Long fileId) {
        fileService.deleteFile(tenantId, fileId);
        return ResponseEntity.noContent().build();
    }
}
