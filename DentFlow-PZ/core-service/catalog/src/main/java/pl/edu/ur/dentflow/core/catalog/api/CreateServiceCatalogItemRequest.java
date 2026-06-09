package pl.edu.ur.dentflow.core.catalog.api;

import jakarta.validation.constraints.*;

public record CreateServiceCatalogItemRequest(
        @NotBlank(message = "Service name is required")
        @Size(max = 100)
        String name,

        @NotNull(message = "Duration is required")
        @Min(value = 5, message = "Minimum duration is 5 minutes")
        Integer durationMinutes,

        @NotNull(message = "Price is required")
        @Min(value = 0, message = "Price cannot be negative")
        Integer priceCents,

        Boolean active
) {}
