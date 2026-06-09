package pl.edu.ur.dentflow.core.audit.infrastructure;

import pl.edu.ur.dentflow.core.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTenantId(Long tenantId);
    List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);
    List<AuditLog> findByTenantIdOrderByTimestampDesc(Long tenantId);
}
