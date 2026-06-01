package com.dentflow.core.clinic.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTenantRequest(
        @NotBlank @Size(max = 100)
        String name,
        @Size(max = 1000)
        String logoUrl,
        String locationName,
        String addressStreet,
        String addressCity,
        String addressZip,
        String addressCountry
) {}
