package com.dentflow.core.catalog.api;

import jakarta.validation.constraints.*;

public record UpdateServiceCatalogItemRequest(
        @NotBlank(message = "Nazwa usługi jest wymagana")
        @Size(max = 100)
        String name,

        @NotNull(message = "Czas trwania jest wymagany")
        @Min(value = 5, message = "Minimalny czas trwania to 5 minut")
        Integer durationMinutes,

        @NotNull(message = "Cena jest wymagana")
        @Min(value = 0, message = "Cena nie może być ujemna")
        Integer priceCents,

        @NotNull
        Boolean active
) {}
