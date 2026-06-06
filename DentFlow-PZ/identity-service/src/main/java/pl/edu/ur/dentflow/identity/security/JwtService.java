package pl.edu.ur.dentflow.identity.security;

import pl.edu.ur.dentflow.identity.user.domain.Role;
import pl.edu.ur.dentflow.identity.user.domain.User;
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

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

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

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

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

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
