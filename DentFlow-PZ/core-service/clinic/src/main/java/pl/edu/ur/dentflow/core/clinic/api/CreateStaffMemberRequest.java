package pl.edu.ur.dentflow.core.clinic.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record CreateStaffMemberRequest(
        Long userId,

        @NotBlank @Size(max = 100)
        String firstName,

        @NotBlank @Size(max = 100)
        String lastName,

        @NotBlank @Size(max = 20)
        String profession,

        String bio,

        String avatarUrl,

        @Size(max = 20) @Pattern(regexp = "^\\+?[0-9][\\s\\-]?([0-9][\\s\\-]?){8,14}$", message = "Nieprawidłowy numer telefonu")
        String phone,

        @Email @Size(max = 255)
        String email,

        LocalTime workingHoursStart,

        LocalTime workingHoursEnd
) {}
