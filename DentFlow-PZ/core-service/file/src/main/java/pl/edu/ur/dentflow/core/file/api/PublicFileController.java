package pl.edu.ur.dentflow.core.file.api;

import pl.edu.ur.dentflow.core.file.application.FileService;
import pl.edu.ur.dentflow.core.file.domain.FileMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public file download endpoint (no authentication required).
 *
 * <p>Files are accessible via their public URL which includes the fileId.
 * This endpoint serves file content with inline disposition for browser
 * rendering (e.g., images, PDFs).</p>
 *
 * @see pl.edu.ur.dentflow.core.file.application.FileService
 */
@RestController
@RequestMapping("/public/files")
public class PublicFileController {

    private final FileService fileService;

    public PublicFileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> downloadPublicFile(@PathVariable Long fileId) {
        FileMetadata meta = fileService.getFileMetadataEntity(fileId);
        byte[] bytes = fileService.downloadFileBytes(meta);

        String contentType = meta.getContentType() != null
                ? meta.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + meta.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }
}
