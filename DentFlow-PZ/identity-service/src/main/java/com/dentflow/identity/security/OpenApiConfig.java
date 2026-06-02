package com.dentflow.identity.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguracja dokumentacji OpenAPI (Swagger UI) dla modułu tożsamości.
 *
 * <p>Rejestruje schemat bezpieczeństwa {@code bearerAuth}, który umożliwia
 * podanie tokenu JWT bezpośrednio w Swagger UI — przycisk
 * <em>Authorize</em> przyjmuje token i dołącza nagłówek
 * {@code Authorization: Bearer <token>} do wszystkich
 * testowanych żądań.</p>
 */

@Configuration
public class OpenApiConfig {

    /**
     * Tworzy instancję {@link OpenAPI} ze zdefiniowanym schematem JWT.
     *
     * <p>Schemat {@code bearerAuth} musi być następnie wskazany
     * w adnotacji {@code @SecurityRequirement(name = "bearerAuth")}
     * na poziomie kontrolera lub metody, aby Swagger UI wymagał
     * tokenu dla danego endpointu.</p>
     *
     * @return skonfigurowany obiekt {@link OpenAPI}
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
