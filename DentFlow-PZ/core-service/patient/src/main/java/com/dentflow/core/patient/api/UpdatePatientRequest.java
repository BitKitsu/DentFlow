package com.dentflow.core.patient.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

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

        String notes,

        LocalDate dateOfBirth,

        @Size(max = 11)
        String pesel,

        @Size(max = 10)
        String gender,

        @Size(max = 100)
        String addressStreet,

        @Size(max = 100)
        String addressCity,

        @Size(max = 20)
        String addressZip,

        @Size(max = 100)
        String addressCountry,

        String avatarUrl
) {}
