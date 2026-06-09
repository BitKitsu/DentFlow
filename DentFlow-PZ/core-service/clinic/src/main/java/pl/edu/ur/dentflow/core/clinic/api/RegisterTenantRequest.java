package pl.edu.ur.dentflow.core.clinic.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterTenantRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String locationName,
        @Size(max = 100) String addressStreet,
        @Size(max = 100) String addressCity,
        @Size(max = 20) String addressZip,
        @Size(max = 100) String addressCountry
) {}
