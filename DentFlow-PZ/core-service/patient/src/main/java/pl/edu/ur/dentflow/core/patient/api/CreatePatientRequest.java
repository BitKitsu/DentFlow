package pl.edu.ur.dentflow.core.patient.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreatePatientRequest(
        Long userId,

        @NotBlank @Size(min = 2, max = 50)
        String firstName,

        @NotBlank @Size(min = 2, max = 50)
        String lastName,

        @Size(max = 20) @Pattern(regexp = "^\\+?[0-9][\\s\\-]?([0-9][\\s\\-]?){8,14}$", message = "Nieprawidłowy numer telefonu")
        String phone,

        @Email @Size(max = 255)
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
