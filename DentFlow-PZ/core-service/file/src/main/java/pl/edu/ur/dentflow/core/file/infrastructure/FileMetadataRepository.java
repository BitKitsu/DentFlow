package pl.edu.ur.dentflow.core.file.infrastructure;

import pl.edu.ur.dentflow.core.file.domain.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByTenantId(Long tenantId);
    List<FileMetadata> findByTenantIdIsNull();
    Optional<FileMetadata> findByIdAndTenantId(Long id, Long tenantId);
    List<FileMetadata> findByUploadedBy(Long uploadedBy);
}
