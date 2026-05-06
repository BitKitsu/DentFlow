package com.dentflow.core.catalog.api;

import lombok.Builder;

@Builder
public record ServiceCatalogItemDTO(
        Long id,
        Long tenantId,
        String name,
        Integer durationMinutes,
        Integer priceCents,
        Boolean active
) {}
