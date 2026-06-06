package pl.edu.ur.dentflow.core.clinic.api;

import jakarta.validation.constraints.NotBlank;

public record AddLocationRequest(
        @NotBlank String name,
        String addressStreet,
        String addressCity,
        String addressZip,
        String addressCountry
) {}
