package com.dentflow.identity.auth.api;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        Long tenantId,
        String firstName,
        String lastName,
        String phone,
        String addressStreet,
        String addressCity,
        String addressZip,
        String addressCountry
) {}
