package com.dentflow.core.catalog.application;

import com.dentflow.core.catalog.api.ServiceCatalogItemDTO;
import com.dentflow.core.catalog.infrastructure.ServiceCatalogItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final ServiceCatalogItemRepository repository;

    public CatalogService(ServiceCatalogItemRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ServiceCatalogItemDTO> getServices(Long tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(item -> ServiceCatalogItemDTO.builder()
                        .id(item.getId())
                        .tenantId(item.getTenantId())
                        .name(item.getName())
                        .durationMinutes(item.getDurationMinutes())
                        .priceCents(item.getPriceCents())
                        .active(item.getActive())
                        .build())
                .collect(Collectors.toList());
    }
}
