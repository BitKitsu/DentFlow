package pl.edu.ur.dentflow.core.clinic.api;

public record SyncFromUserRequest(
        Long userId,
        String firstName,
        String lastName,
        String avatarUrl,
        String phone,
        String email
) {}
