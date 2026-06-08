package pl.edu.ur.dentflow.core.catalog.application;

import pl.edu.ur.dentflow.core.catalog.api.CreateServiceCatalogItemRequest;
import pl.edu.ur.dentflow.core.catalog.api.ServiceCatalogItemDTO;
import pl.edu.ur.dentflow.core.catalog.api.UpdateServiceCatalogItemRequest;
import pl.edu.ur.dentflow.core.catalog.domain.ServiceCatalogItem;
import pl.edu.ur.dentflow.core.catalog.infrastructure.ServiceCatalogItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final ServiceCatalogItemRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public CatalogService(ServiceCatalogItemRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<ServiceCatalogItemDTO> getAllServices(Long tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceCatalogItemDTO> getActiveServices(Long tenantId) {
        return repository.findByTenantIdAndActiveTrue(tenantId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceCatalogItemDTO> getAllActiveServices() {
        return repository.findAll().stream()
                .filter(ServiceCatalogItem::getActive)
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceCatalogItemDTO getService(Long tenantId, Long id) {
        return repository.findByIdAndTenantId(id, tenantId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Service with id=" + id + " does not exist in this clinic"));
    }

    @Transactional
    public ServiceCatalogItemDTO createService(Long tenantId, CreateServiceCatalogItemRequest request) {
        ServiceCatalogItem item = ServiceCatalogItem.builder()
                .tenantId(tenantId)
                .name(request.name())
                .durationMinutes(request.durationMinutes())
                .priceCents(request.priceCents())
                .active(request.active() != null ? request.active() : true)
                .build();
        return toDTO(repository.save(item));
    }

    @Transactional
    public ServiceCatalogItemDTO updateService(Long tenantId, Long id, UpdateServiceCatalogItemRequest request) {
        ServiceCatalogItem item = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Service with id=" + id + " does not exist in this clinic"));

        item.setName(request.name());
        item.setDurationMinutes(request.durationMinutes());
        item.setPriceCents(request.priceCents());
        item.setActive(request.active());

        return toDTO(repository.save(item));
    }

    @Transactional
    public void deleteService(Long tenantId, Long id) {
        ServiceCatalogItem item = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Service with id=" + id + " does not exist in this clinic"));

        int cancelled = jdbcTemplate.update(
                "UPDATE appointment SET status = 'CANCELLED' " +
                "WHERE tenant_id = ? AND service_item_id = ? AND status NOT IN ('CANCELLED', 'COMPLETED')",
                tenantId, id);
        if (cancelled > 0) {
            log.info("Cancelled {} appointments using deleted service {} for tenant {}", cancelled, id, tenantId);
        }

        repository.delete(item);
    }

    private ServiceCatalogItemDTO toDTO(ServiceCatalogItem item) {
        return new ServiceCatalogItemDTO(
                item.getId(),
                item.getTenantId(),
                item.getName(),
                item.getDurationMinutes(),
                item.getPriceCents(),
                item.getActive()
        );
    }
}
