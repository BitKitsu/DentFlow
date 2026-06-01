package com.dentflow.identity.user.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Encja przypisania roli do użytkownika.
 *
 * <p>Reprezentuje wiersz w tabeli {@code user_role} i modeluje
 * relację wiele-do-wielu między {@link User} a {@link Role}
 * jako osobną encję — co pozwala na ewentualne dodanie
 * dodatkowych pól (np. daty przypisania, tenantId) bez
 * zmiany schematu.</p>
 */
@Entity
@Table(name = "user_role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    /**
     * Klucz główny generowany przez bazę danych.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Użytkownik, któremu przypisana jest rola.
     *
     * <p>Relacja ładowana leniwie ({@code LAZY}) — dostęp do pola
     * poza aktywną transakcją spowoduje {@code LazyInitializationException}.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Użytkownik, któremu przypisana jest rola.
     *
     * <p>Relacja ładowana leniwie ({@code LAZY}) — dostęp do pola
     * poza aktywną transakcją spowoduje {@code LazyInitializationException}.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
}
