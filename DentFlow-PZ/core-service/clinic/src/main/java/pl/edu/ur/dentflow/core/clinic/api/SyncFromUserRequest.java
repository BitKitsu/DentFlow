package pl.edu.ur.dentflow.core.clinic.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record SyncFromUserRequest(
        Long userId,
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 1000) String avatarUrl,
        @Size(max = 20) String phone,
        @Email @Size(max = 255) String email
) {}
