package com.dentflow.core.patient.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePatientRequest(
        Long userId,

        @NotBlank @Size(max = 50)
        String firstName,

        @NotBlank @Size(max = 50)
        String lastName,

        @Size(max = 20)
        String phone,

        @Size(max = 255)
        String email,

        String notes
) {}
