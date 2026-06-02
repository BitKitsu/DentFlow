package com.dentflow.identity.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Konfiguracja Spring Security dla modułu tożsamości.
 *
 * <p>Definiuje bezstanowy łańcuch filtrów oparty na tokenach JWT.
 * Sesje HTTP są wyłączone — każde żądanie musi zawierać ważny token
 * w nagłówku {@code Authorization: Bearer <token>}.</p>
 *
 * <p>Kolejność filtrów przed {@link UsernamePasswordAuthenticationFilter}:</p>
 * <ol>
 *   <li>{@link RateLimitFilter} — ogranicza liczbę żądań z jednego IP</li>
 *   <li>{@link JwtAuthenticationFilter} — weryfikuje token i ustawia kontekst bezpieczeństwa</li>
 * </ol>
 *
 * <p>Endpointy publiczne (niewymagające tokenu):</p>
 * <ul>
 *   <li>{@code /auth/**} — rejestracja i logowanie</li>
 *   <li>{@code /swagger-ui/**}, {@code /api-docs/**} — dokumentacja API</li>
 *   <li>{@code /error} — obsługa błędów Spring</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    /**
     * Tworzy konfigurację z wstrzykniętymi filtrami bezpieczeństwa.
     *
     * @param jwtAuthenticationFilter filtr weryfikujący tokeny JWT
     * @param rateLimitFilter         filtr ograniczający częstotliwość żądań
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RateLimitFilter rateLimitFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    /**
     * Buduje i rejestruje łańcuch filtrów bezpieczeństwa.
     *
     * <p>Konfiguracja:</p>
     * <ul>
     *   <li>CSRF wyłączony — aplikacja jest bezstanowym API, nie obsługuje formularzy</li>
     *   <li>Zarządzanie sesją: {@code STATELESS} — brak sesji HTTP po stronie serwera</li>
     *   <li>Wszystkie żądania spoza listy publicznej wymagają uwierzytelnienia</li>
     * </ul>
     *
     * @param http obiekt konfiguracji HTTP dostarczany przez Spring Security
     * @return skonfigurowany {@link SecurityFilterChain}
     * @throws Exception gdy konfiguracja HTTP jest nieprawidłowa
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/swagger-ui/**", "/api-docs/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Rejestruje enkoder haseł oparty na algorytmie BCrypt.
     *
     * <p>BCrypt automatycznie generuje sól i jest odporny na ataki
     * słownikowe dzięki regulowanemu współczynnikowi kosztu.
     * Bean jest używany przez serwisy rejestracji i uwierzytelniania.</p>
     *
     * @return instancja {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
