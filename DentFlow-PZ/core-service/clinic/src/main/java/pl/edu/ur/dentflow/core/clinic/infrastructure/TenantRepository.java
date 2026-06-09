package pl.edu.ur.dentflow.core.clinic.infrastructure;

import pl.edu.ur.dentflow.core.clinic.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}
