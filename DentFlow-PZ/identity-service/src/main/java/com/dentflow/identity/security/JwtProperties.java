package com.dentflow.identity.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Właściwości konfiguracyjne tokenu JWT, wczytywane z pliku
 * {@code application.yml} (lub {@code .properties}) pod prefiksem {@code jwt}.
 *
 * <p>Przykładowa konfiguracja w {@code application.yml}:</p>
 * <pre>{@code
 * jwt:
 *   secret: twoj-sekret-minimum-32-znaki-dla-hmac256
 *   expiration: 86400000   # 24 godziny w milisekundach
 * }</pre>
 *
 * <p><b>Uwaga:</b> wartość {@code secret} nie powinna być commitowana
 * do repozytorium — używaj zmiennych środowiskowych lub zewnętrznego
 * vault (np. {@code JWT_SECRET} przez {@code ${JWT_SECRET}}).</p>
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long expiration;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getExpiration() { return expiration; }
    public void setExpiration(long expiration) { this.expiration = expiration; }
}
