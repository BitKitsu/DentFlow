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
import com.dentflow.identity.auth.api.ChangePasswordRequest;
import com.dentflow.identity.auth.api.UpdateProfileRequest;
import org.springframework.security.core.Authentication;

/**
 * Kontroler REST obsługujący uwierzytelnianie i zarządzanie kontem użytkownika.
 *
 * <p>Wszystkie endpointy są dostępne pod ścieżką {@code /auth}.
 * Endpointy wymagające zalogowania pobierają tożsamość użytkownika
 * z obiektu {@link Authentication} wypełnianego przez
 * {@link com.dentflow.identity.security.JwtAuthenticationFilter}
 * — {@code authentication.getName()} zwraca e-mail z claima {@code sub}.</p>
 *
 * <p>Endpointy publiczne (bez tokenu):</p>
 * <ul>
 *   <li>{@code POST /auth/register}</li>
 *   <li>{@code POST /auth/login}</li>
 *   <li>{@code POST /auth/logout}</li>
 * </ul>
 *
 * <p>Endpointy chronione ({@code Authorization: Bearer <token>}):</p>
 * <ul>
 *   <li>{@code POST /auth/tenant}</li>
 *   <li>{@code PUT /auth/change-password}</li>
 *   <li>{@code PUT /auth/profile}</li>
 *   <li>{@code DELETE /auth/account}</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Rejestracja i logowanie")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    
    /**
     * Tworzy kontroler z wstrzykniętym serwisem uwierzytelniania.
     *
     * @param authService serwis obsługujący logikę rejestracji, logowania i konta
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Rejestruje nowy gabinet wraz z kontem właściciela.
     *
     * <p>Nowe konto otrzymuje domyślnie rolę {@code OWNER}.
     * Tenant jest przypisywany osobno przez {@code POST /auth/tenant}.</p>
     *
     * @param request dane rejestracyjne; walidowane przez Bean Validation
     * @return {@code 201 Created} z {@link AuthResponse} zawierającym token JWT
     * @throws org.springframework.web.server.ResponseStatusException
     *         {@code 409 Conflict} gdy e-mail jest już zajęty
     */
    @PostMapping("/register")
    @Operation(summary = "Rejestracja nowego gabinetu i konta właściciela")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Otrzymano żądanie rejestracji dla email: {}", request.email());
        AuthResponse response = authService.register(request);
        log.info("Rejestracja zakończona sukcesem dla użytkownika id: {}, email: {}", response.userId(), response.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Uwierzytelnia użytkownika i zwraca token JWT.
     *
     * @param request dane logowania (e-mail i hasło); walidowane przez Bean Validation
     * @return {@code 200 OK} z {@link AuthResponse} zawierającym token JWT
     * @throws org.springframework.web.server.ResponseStatusException
     *         {@code 401 Unauthorized} gdy dane są nieprawidłowe,
     *         {@code 403 Forbidden} gdy konto jest nieaktywne
     */
    @PostMapping("/login")
    @Operation(summary = "Logowanie użytkownika - zwraca JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Otrzymano żądanie logowania dla email: {}", request.email());
        AuthResponse response = authService.login(request);
        log.info("Logowanie zakończone sukcesem dla użytkownika id: {}, email: {}", response.userId(), response.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Wylogowuje użytkownika po stronie klienta.
     *
     * <p>JWT jest bezstanowy — serwer nie przechowuje sesji ani listy
     * unieważnionych tokenów. Endpoint zwraca {@code 204 No Content},
     * a klient jest odpowiedzialny za usunięcie tokenu ze swojego magazynu
     * (np. SharedPreferences, localStorage).</p>
     *
     * <p>Jeśli w przyszłości wymagane będzie natychmiastowe unieważnianie tokenów,
     * należy wprowadzić token blacklist (np. w Redis) z TTL równym czasowi
     * wygaśnięcia tokenu.</p>
     *
     * @return {@code 204 No Content}
     */
    @PostMapping("/logout")
    @Operation(summary = "Wylogowanie - klient usuwa JWT lokalnie")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout() {
        log.info("Otrzymano żądanie wylogowania");
        return ResponseEntity.noContent().build();
    }

    /**
     * Przypisuje gabinet (tenant) do konta zalogowanego użytkownika.
     *
     * <p>Po pomyślnym przypisaniu zwracany jest odświeżony token JWT
     * zawierający nowy {@code tenantId} — klient powinien zastąpić
     * dotychczasowy token nowym.</p>
     *
     * @param request        żądanie z {@code tenantId} do przypisania
     * @param authentication kontekst bezpieczeństwa zalogowanego użytkownika
     * @return {@code 200 OK} z {@link AuthResponse} zawierającym odświeżony token JWT
     * @throws org.springframework.web.server.ResponseStatusException
     *         {@code 404 Not Found} gdy użytkownik nie istnieje
     */
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

    /**
     * Zmienia hasło zalogowanego użytkownika.
     *
     * <p>Wymaga podania aktualnego hasła w celu weryfikacji tożsamości.</p>
     *
     * @param request        żądanie ze starym i nowym hasłem; walidowane przez Bean Validation
     * @param authentication kontekst bezpieczeństwa zalogowanego użytkownika
     * @return {@code 204 No Content} po pomyślnej zmianie
     * @throws org.springframework.web.server.ResponseStatusException
     *         {@code 401 Unauthorized} gdy aktualne hasło jest nieprawidłowe
     */
    @PutMapping("/change-password")
    @Operation(summary = "Zmiana hasła zalogowanego użytkownika")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Aktualizuje dane profilowe zalogowanego użytkownika.
     *
     * <p>Wszystkie pola żądania są opcjonalne — tylko pola z wartością
     * inną niż {@code null} są nadpisywane. Po zmianie e-maila
     * zwrócony token zawiera nowy adres jako claim {@code sub}.</p>
     *
     * @param request        nowe dane profilowe (wszystkie pola opcjonalne)
     * @param authentication kontekst bezpieczeństwa zalogowanego użytkownika
     * @return {@code 200 OK} z {@link AuthResponse} zawierającym odświeżony token JWT
     * @throws org.springframework.web.server.ResponseStatusException
     *         {@code 409 Conflict} gdy nowy e-mail jest już zajęty
     */
    @PutMapping("/profile")
    @Operation(summary = "Aktualizacja danych profilowych zalogowanego użytkownika")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AuthResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        AuthResponse response = authService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Trwale usuwa konto zalogowanego użytkownika.
     *
     * <p>Operacja jest nieodwracalna. Kaskadowo usuwa również wszystkie
     * role powiązane z kontem. Po wykonaniu klient powinien usunąć
     * token JWT ze swojego magazynu.</p>
     *
     * @param authentication kontekst bezpieczeństwa zalogowanego użytkownika
     * @return {@code 204 No Content} po pomyślnym usunięciu konta
     * @throws org.springframework.web.server.ResponseStatusException
     *         {@code 404 Not Found} gdy użytkownik nie istnieje
     */
    @DeleteMapping("/account")
    @Operation(summary = "Usunięcie konta zalogowanego użytkownika")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        authService.deleteAccount(authentication.getName());
        return ResponseEntity.noContent().build();
    }

}
