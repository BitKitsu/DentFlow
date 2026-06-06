package pl.edu.ur.dentflow.core.catalog.infrastructure;

import pl.edu.ur.dentflow.core.catalog.domain.ServiceCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCatalogItemRepository extends JpaRepository<ServiceCatalogItem, Long> {
    List<ServiceCatalogItem> findByTenantId(Long tenantId);
    List<ServiceCatalogItem> findByTenantIdAndActiveTrue(Long tenantId);
    Optional<ServiceCatalogItem> findByIdAndTenantId(Long id, Long tenantId);
}
