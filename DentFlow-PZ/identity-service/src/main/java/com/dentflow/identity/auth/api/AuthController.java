package com.dentflow.identity.auth.api;

import com.dentflow.identity.auth.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dentflow.identity.auth.api.AssignTenantRequest;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Rejestracja i logowanie")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Rejestracja nowego gabinetu i konta właściciela")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Logowanie użytkownika - zwraca JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
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
