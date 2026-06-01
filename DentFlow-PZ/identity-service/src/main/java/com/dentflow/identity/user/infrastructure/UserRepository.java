package com.dentflow.identity.user.infrastructure;

import com.dentflow.identity.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozytorium dostępu do danych użytkowników.
 *
 * <p>Rozszerza {@link JpaRepository}, dostarczając standardowe operacje
 * CRUD dla encji {@link User}, oraz definiuje zapytania specyficzne
 * dla domeny uwierzytelniania.</p>
 */

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Wyszukuje użytkownika po adresie e-mail.
     *
     * <p>Używane przede wszystkim podczas uwierzytelniania —
     * e-mail pełni rolę loginu w systemie.</p>
     *
     * @param email adres e-mail do wyszukania
     * @return {@link Optional} z użytkownikiem, lub pusty jeśli nie istnieje
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Sprawdza czy użytkownik o podanym adresie e-mail już istnieje.
     *
     * <p>Używane podczas rejestracji do weryfikacji unikalności e-maila
     * przed próbą zapisu — pozwala uniknąć naruszenia ograniczenia
     * {@code UNIQUE} na poziomie bazy danych.</p>
     *
     * @param email adres e-mail do sprawdzenia
     * @return {@code true} jeśli e-mail jest już zajęty, {@code false} w przeciwnym razie
     */
    boolean existsByEmail(String email);
}
