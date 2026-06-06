package pl.edu.ur.dentflow.identity.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Partial update of the logged-in user's profile.
 * All fields are optional — only non-null values are applied.
 */
public record UpdateProfileRequest(
        @Email String email,
        @Size(min = 2, max = 100) String firstName,
        @Size(min = 2, max = 100) String lastName,
        @Pattern(regexp = "^\\+?[0-9][\\s\\-]?([0-9][\\s\\-]?){8,14}$",
                 message = "Invalid phone number format") String phone,
        @Size(max = 100) String addressStreet,
        @Size(max = 100) String addressCity,
        @Size(max = 20)  String addressZip,
        @Size(max = 50)  String addressCountry,
        @Size(max = 1000) String avatarUrl
) {}
