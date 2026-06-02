package com.dentflow.identity.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Żądanie logowania do systemu.
 *
 * <p>Backend celowo zwraca ten sam komunikat błędu ({@code "Nieprawidłowy email lub hasło"})
 * zarówno przy błędnym e-mailu, jak i przy niepoprawnym haśle. Zapobiega to enumeracji
 * kont przez atakującego.</p>
 */
public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {}
