package pl.edu.ur.dentflow.identity.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registration request for a new OWNER account.
 * Clinic data (name, location) are sent separately to core-service POST /tenants/register.
 */
public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String password,
        @Size(min = 2, max = 100) String firstName,
        @Size(min = 2, max = 100) String lastName,
        @Pattern(regexp = "^\\+?[0-9][\\s\\-]?([0-9][\\s\\-]?){8,14}$",
                 message = "Invalid phone number format") String phone,
        @Size(max = 100) String addressStreet,
        @Size(max = 100) String addressCity,
        @Size(max = 20)  String addressZip,
        @Size(max = 50)  String addressCountry
) {}
