package com.dentflow.identity.auth.api;

import com.dentflow.identity.auth.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dentflow.identity.auth.api.AssignTenantRequest;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Rejestracja i logowanie")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Rejestracja nowego gabinetu i konta właściciela")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Otrzymano żądanie rejestracji dla email: {}", request.email());
        AuthResponse response = authService.register(request);
        log.info("Rejestracja zakończona sukcesem dla użytkownika id: {}, email: {}", response.userId(), response.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Logowanie użytkownika - zwraca JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Otrzymano żądanie logowania dla email: {}", request.email());
        AuthResponse response = authService.login(request);
        log.info("Logowanie zakończone sukcesem dla użytkownika id: {}, email: {}", response.userId(), response.email());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/logout
     * JWT jest bezstanowy - serwer nie przechowuje sesji.
     * Klient powinien usunąć token lokalnie. Endpoint zwraca 204 No Content.
     */
    @PostMapping("/logout")
    @Operation(summary = "Wylogowanie - klient usuwa JWT lokalnie")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout() {
        log.info("Otrzymano żądanie wylogowania");
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/tenant")
    @Operation(summary = "Przypisz tenantId aktualnemu użytkownikowi")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AuthResponse> assignTenant(
            @Valid @RequestBody AssignTenantRequest request,
            Authentication authentication) {
        AuthResponse response = authService.assignTenantToCurrentUser(
                authentication.getName(), request.tenantId());
        return ResponseEntity.ok(response);
    }
    
}
