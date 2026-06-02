package com.dentflow.identity.security;

import com.dentflow.identity.user.domain.Role;
import com.dentflow.identity.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Serwis obsługujący generowanie i walidację tokenów JWT.
 *
 * <p>Token zawiera następujące claims:</p>
 * <ul>
 *   <li>{@code sub} — adres e-mail użytkownika (subject)</li>
 *   <li>{@code userId} — identyfikator użytkownika ({@code Long})</li>
 *   <li>{@code tenantId} — identyfikator gabinetu ({@code Long})</li>
 *   <li>{@code roles} — lista ról jako nazwy stałych {@link Role}, np. {@code ["OWNER", "DENTIST"]}</li>
 * </ul>
 *
 * <p>Token jest podpisywany algorytmem HMAC-SHA przy użyciu klucza
 * skonfigurowanego w {@link JwtProperties}. Czas wygaśnięcia również
 * pochodzi z konfiguracji.</p>
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties jwtProperties;

    /**
     * Tworzy serwis z wstrzykniętą konfiguracją JWT.
     *
     * @param jwtProperties właściwości JWT (sekret, czas wygaśnięcia)
     */

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * Generuje podpisany token JWT dla podanego użytkownika.
     *
     * <p>Role są mapowane z obiektów {@link com.dentflow.identity.user.domain.UserRole}
     * na listę nazw stałych enumeracji {@link Role}, np. {@code "OWNER"}.</p>
     *
     * @param user użytkownik, dla którego generowany jest token
     * @return podpisany token JWT jako {@code String}
     */
    public String generateToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getRole().name())
                .toList();

        log.info("Generowanie tokenu JWT dla użytkownika - email: {}, role: {}", user.getEmail(), roles);

        String token = Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("tenantId", user.getTenantId())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(getSigningKey())
                .compact();

        log.info("Token JWT wygenerowany pomyślnie dla email: {}", user.getEmail());
        return token;
    }

    /**
     * Parsuje token i zwraca wszystkie zawarte w nim claims.
     *
     * @param token token JWT do sparsowania
     * @return obiekt {@link Claims} z wszystkimi claimami tokenu
     * @throws io.jsonwebtoken.JwtException gdy token jest nieprawidłowy,
     *         nieważny lub wygasły
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    /**
     * Wyciąga adres e-mail użytkownika z tokenu (claim {@code sub}).
     *
     * @param token token JWT
     * @return adres e-mail zawarty w tokenie
     * @throws io.jsonwebtoken.JwtException gdy token jest nieprawidłowy lub wygasły
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Weryfikuje czy token jest ważny dla podanego adresu e-mail.
     *
     * <p>Token jest uznawany za ważny gdy spełnione są oba warunki:</p>
     * <ul>
     *   <li>claim {@code sub} jest równy podanemu {@code email}</li>
     *   <li>token nie wygasł</li>
     * </ul>
     *
     * <p>Wszelkie wyjątki podczas parsowania (wygaśnięcie, zły podpis,
     * uszkodzony format) są przechwytywane i logowane — metoda zwraca
     * wtedy {@code false} zamiast propagować wyjątek.</p>
     *
     * @param token token JWT do zweryfikowania
     * @param email oczekiwany adres e-mail właściciela tokenu
     * @return {@code true} jeśli token jest ważny, {@code false} w przeciwnym razie
     */
    public boolean isTokenValid(String token, String email) {
        try {
            String tokenEmail = extractEmail(token);
            boolean valid = tokenEmail.equals(email) && !isTokenExpired(token);
            if (!valid) {
                log.warn("Token JWT nieprawidłowy dla email: {}", email);
            }
            return valid;
        } catch (ExpiredJwtException e) {
            log.error("Token JWT wygasł dla email: {}", email);
            return false;
        } catch (Exception e) {
            log.error("Błąd walidacji tokenu JWT dla email: {} - {}", email, e.getMessage());
            return false;
        }
    }

   /**
     * Sprawdza czy token wygasł porównując datę ekspiracji z aktualnym czasem.
     *
     * @param token token JWT
     * @return {@code true} jeśli token wygasł, {@code false} jeśli jest jeszcze ważny
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Buduje klucz podpisujący HMAC-SHA na podstawie sekretu z konfiguracji.
     *
     * <p>Sekret pobierany jest jako surowe bajty stringa z {@link JwtProperties}.
     * Minimalna wymagana długość klucza dla HMAC-SHA256 to 32 bajty (256 bitów)
     * — krótszy sekret spowoduje wyjątek przy starcie aplikacji.</p>
     *
     * @return {@link SecretKey} gotowy do podpisywania i weryfikacji tokenów
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
