package com.dentflow.core.catalog.api;

public record ServiceCatalogItemDTO(
        Long id,
        Long tenantId,
        String name,
        Integer durationMinutes,
        Integer priceCents,
        Boolean active
) {}
