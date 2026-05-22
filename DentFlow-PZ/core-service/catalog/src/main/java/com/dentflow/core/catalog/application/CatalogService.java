package com.dentflow.core.catalog.application;

import com.dentflow.core.catalog.api.CreateServiceCatalogItemRequest;
import com.dentflow.core.catalog.api.ServiceCatalogItemDTO;
import com.dentflow.core.catalog.api.UpdateServiceCatalogItemRequest;
import com.dentflow.core.catalog.domain.ServiceCatalogItem;
import com.dentflow.core.catalog.infrastructure.ServiceCatalogItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CatalogService {

    private final ServiceCatalogItemRepository repository;

    public CatalogService(ServiceCatalogItemRepository repository) {
        this.repository = repository;
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
    public ServiceCatalogItemDTO getService(Long tenantId, Long id) {
        return repository.findByIdAndTenantId(id, tenantId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usługa o id=" + id + " nie istnieje w tym gabinecie"));
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
                        HttpStatus.NOT_FOUND, "Usługa o id=" + id + " nie istnieje w tym gabinecie"));

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
                        HttpStatus.NOT_FOUND, "Usługa o id=" + id + " nie istnieje w tym gabinecie"));
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
