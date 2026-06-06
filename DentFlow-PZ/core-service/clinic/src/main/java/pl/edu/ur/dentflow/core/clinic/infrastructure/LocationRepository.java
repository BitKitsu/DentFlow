package pl.edu.ur.dentflow.core.clinic.infrastructure;

import pl.edu.ur.dentflow.core.clinic.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByTenantId(Long tenantId);
}
