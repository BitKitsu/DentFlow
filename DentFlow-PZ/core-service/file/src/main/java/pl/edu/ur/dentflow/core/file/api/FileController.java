package pl.edu.ur.dentflow.core.file.api;

import pl.edu.ur.dentflow.core.file.application.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * File management endpoints with S3 integration.
 * SCRUM-64
 *
 * POST   /tenants/{tenantId}/files              – upload file
 * GET    /tenants/{tenantId}/files              – file list
 * GET    /tenants/{tenantId}/files/{fileId}     – file metadata
 * GET    /tenants/{tenantId}/files/{fileId}/download – download file
 * DELETE /tenants/{tenantId}/files/{fileId}     – delete file
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
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        String host = request.getHeader("Host");
        String proto = request.getHeader("X-Forwarded-Proto");
        FileUploadResponse response = fileService.uploadFile(tenantId, uploadedByUserId, file, host, proto);
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
