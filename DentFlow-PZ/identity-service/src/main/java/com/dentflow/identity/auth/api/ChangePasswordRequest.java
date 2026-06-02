package com.dentflow.identity.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Żądanie zmiany hasła dla zalogowanego użytkownika.
 *
 * <p>Wymaga podania zarówno aktualnego, jak i nowego hasła w celu
 * weryfikacji tożsamości i zapobiegania przejęciu aktywnej sesji.
 * Nowe hasło musi spełniać {@link com.dentflow.identity.config.SecurityConfig#passwordEncoder}
 * — obecnie {@code min 8 znaków} oraz {@code PasswordPolicyValidator}.</p>
 *
 * @see com.dentflow.identity.auth.application.AuthService#changePassword
 */
public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, message = "Nowe hasło musi mieć co najmniej 8 znaków") String newPassword
) {}
