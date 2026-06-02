package com.dentflow.identity.auth.api;

/**
 * Odpowiedź zwracana po pomyślnym uwierzytelnieniu lub aktualizacji profilu.
 *
 * <p>Zwracana przez {@code POST /auth/register}, {@code POST /auth/login}
 * oraz {@code PUT /auth/profile}. Klient powinien zachować {@code token}
 * i dołączać go do kolejnych żądań w nagłówku
 * {@code Authorization: Bearer <token>}.</p>
 *
 * @param token        podpisany token JWT ważny przez czas określony w konfiguracji
 * @param userId       identyfikator konta użytkownika
 * @param email        aktualny adres e-mail użytkownika (może się zmienić po aktualizacji profilu)
 * @param tenantId     identyfikator gabinetu; {@code 0} jeśli tenant nie został jeszcze przypisany
 * @param firstName    imię użytkownika
 * @param lastName     nazwisko użytkownika
 * @param phone        numer telefonu kontaktowego
 * @param addressStreet ulica i numer domu
 * @param addressCity  miasto
 * @param addressZip   kod pocztowy
 * @param addressCountry kraj
 * @param avatarUrl    URL do zdjęcia profilowego; może być {@code null}
 */
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
        String addressCountry,
        String avatarUrl
) {}
