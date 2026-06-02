package com.dentflow.identity.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Żądanie częściowej aktualizacji profilu zalogowanego użytkownika.
 *
 * <p>Wszystkie pola są opcjonalne — wartość {@code null} oznacza
 * brak zmiany danego pola. Tylko pola z wartością inną niż {@code null}
 * są zapisywane przez {@link com.dentflow.identity.auth.application.AuthService#updateProfile}.</p>
 *
 * <p>Zmiana adresu e-mail wymaga unikalności w systemie — jeśli podany
 * e-mail jest już zajęty, serwis zwróci {@code 409 Conflict}.</p>
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
