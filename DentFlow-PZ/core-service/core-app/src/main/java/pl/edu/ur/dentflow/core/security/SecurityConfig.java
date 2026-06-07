package pl.edu.ur.dentflow.core.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for Core Service.
 *
 * <p>Security layers:
 * <ul>
 *   <li>CSRF disabled (stateless API)</li>
 *   <li>Sessions disabled (STATELESS - JWT token)</li>
 *   <li>JWT filtering ({@link JwtAuthenticationFilter}) - token verification</li>
 *   <li>Method security ({@code @EnableMethodSecurity})</li>
 * </ul>
 *
 * <p>Public endpoints (no authorization required):
 * <ul>
 *   <li>/tenants/register - new clinic registration</li>
 *   <li>/tenants - clinic list (marketplace)</li>
 *   <li>/tenants/catalog/all - public service catalog</li>
 *   <li>/tenants/staff/all - public staff list</li>
 *   <li>/public/files/** - public file download</li>
 *   <li>/swagger-ui/**, /api-docs/** - API documentation</li>
 * </ul>
 *
 * <p>OpenAPI configuration includes Bearer JWT authorization scheme.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public: clinic registration, Swagger, file downloads
                        .requestMatchers("/tenants/register", "/tenants", "/tenants/catalog/all", "/tenants/staff/all", "/swagger-ui/**", "/api-docs/**", "/public/files/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

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
