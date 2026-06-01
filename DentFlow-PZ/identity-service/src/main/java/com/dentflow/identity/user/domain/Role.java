package com.dentflow.identity.user.domain;
/**
 * Wyliczenie ról dostępnych w systemie DentFlow.
 *
 * <p>Rola determinuje poziom uprawnień użytkownika w obrębie gabinetu.
 * Przypisywana jest przez encję {@link UserRole} i przechowywana
 * w bazie danych jako {@code VARCHAR} z nazwą stałej, np. {@code "OWNER"}.</p>
 *
 * <ul>
 *   <li>{@link #OWNER} — właściciel gabinetu, pełny dostęp</li>
 *   <li>{@link #DENTIST} — lekarz dentysta, dostęp do wizyt i cennika</li>
 *   <li>{@link #RECEPTIONIST} — recepcjonista, zarządzanie harmonogramem i pacjentami</li>
 *   <li>{@link #ASSISTANT} — asystent, ograniczony dostęp operacyjny</li>
 *   <li>{@link #PATIENT} — pacjent, dostęp wyłącznie do własnych danych</li>
 * </ul>
 */
public enum Role {
    OWNER,
    DENTIST,
    RECEPTIONIST,
    ASSISTANT,
    PATIENT
}
