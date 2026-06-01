package com.dentflow.identity.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Encja reprezentująca konto użytkownika w systemie DentFlow.
 *
 * <p>Nazwa tabeli {@code "user"} jest escapowana cudzysłowami,
 * ponieważ {@code user} jest słowem kluczowym w PostgreSQL.</p>
 *
 * <p>Jeden użytkownik należy do dokładnie jednego tenanta ({@code tenantId})
 * i może posiadać wiele ról ({@link UserRole}). Role są ładowane zachłannie
 * ({@code EAGER}) i kaskadowane razem z encją użytkownika.</p>
 */

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Klucz główny generowany przez bazę danych.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Adres e-mail użytkownika — unikalny w całej tabeli,
     * używany jako login podczas uwierzytelniania.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Imię użytkownika. Maksymalnie 100 znaków.
     */
    @Column(name = "first_name", length = 100)
    private String firstName;

    /**
     * Nazwisko użytkownika. Maksymalnie 100 znaków.
     */
    @Column(name = "last_name", length = 100)
    private String lastName;

    /**
     * Numer telefonu kontaktowego. Maksymalnie 20 znaków.
     */
    @Column(length = 20)
    private String phone;

    /**
     * Ulica i numer domu z adresu użytkownika.
     */
    @Column(name = "address_street", length = 100)
    private String addressStreet;

    /**
     * Miasto z adresu użytkownika.
     */
    @Column(name = "address_city", length = 100)
    private String addressCity;

    /**
     * Kod pocztowy z adresu użytkownika.
     */
    @Column(name = "address_zip", length = 20)
    private String addressZip;

    /**
     * Kraj z adresu użytkownika.
     */
    @Column(name = "address_country", length = 50)
    private String addressCountry;

    /**
     * URL do zdjęcia profilowego (awatara) użytkownika.
     * Maksymalnie 1000 znaków — wystarczy dla typowych URL-i do storage.
     */
    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;
    /**
     * Skrót hasła użytkownika (np. BCrypt).
     * Nigdy nie przechowuje hasła w postaci jawnej.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Identyfikator gabinetu (tenanta), do którego należy użytkownik.
     * Każdy użytkownik jest przypisany do dokładnie jednego tenanta.
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /**
     * Status konta użytkownika.
     *
     * <p>Domyślna wartość to {@code "ACTIVE"}. Inne możliwe wartości
     * zależą od logiki aplikacji, np. {@code "INACTIVE"} lub {@code "BLOCKED"}.</p>
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

     /**
     * Data i czas utworzenia konta, ustawiane automatycznie przez Hibernate.
     * Pole jest niemodyfikowalne po zapisie ({@code updatable = false}).
     * Przechowywane jako {@code TIMESTAMPTZ} (z informacją o strefie czasowej).
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    /**
     * Data i czas ostatniej modyfikacji encji, aktualizowane automatycznie
     * przez Hibernate przy każdym zapisie.
     * Przechowywane jako {@code TIMESTAMPTZ}.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;

    /**
     * Lista ról przypisanych użytkownikowi.
     *
     * <p>Relacja jest zarządzana kaskadowo — usunięcie użytkownika
     * automatycznie usuwa wszystkie jego role ({@code CascadeType.ALL}
     * + {@code orphanRemoval = true}).</p>
     *
     * <p>Role są ładowane zachłannie ({@code EAGER}), co oznacza że każde
     * pobranie użytkownika z bazy zawsze dołącza jego role.
     * Przy dużej liczbie użytkowników rozważ zmianę na {@code LAZY}.</p>
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<UserRole> roles = new ArrayList<>();
}
