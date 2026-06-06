package pl.edu.ur.dentflow.core.clinic.api;

import jakarta.validation.constraints.NotBlank;

public record RegisterTenantRequest(
        @NotBlank String name,
        @NotBlank String locationName,
        String addressStreet,
        String addressCity,
        String addressZip,
        String addressCountry
) {}
