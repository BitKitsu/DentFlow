package pl.edu.ur.dentflow.core.file.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "file_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "public_url", length = 1000)
    private String publicUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;
}
