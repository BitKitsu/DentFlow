package pl.edu.ur.dentflow.identity.auth.api;

import pl.edu.ur.dentflow.identity.auth.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.ur.dentflow.identity.auth.api.AssignTenantRequest;
import pl.edu.ur.dentflow.identity.auth.api.AssignTenantToUserRequest;
import pl.edu.ur.dentflow.identity.auth.api.AssignRoleRequest;
import pl.edu.ur.dentflow.identity.auth.api.ChangePasswordRequest;
import pl.edu.ur.dentflow.identity.auth.api.UpdateProfileRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Registration and login")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new clinic and owner account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.email());
        AuthResponse response = authService.register(request);
        log.info("Registration completed successfully for user id: {}, email: {}", response.userId(), response.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login - returns JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.email());
        AuthResponse response = authService.login(request);
        log.info("Login completed successfully for user id: {}, email: {}", response.userId(), response.email());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/logout
     * JWT is stateless - server does not store sessions.
     * Client should delete token locally. Endpoint returns 204 No Content.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout - client removes JWT locally")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout() {
        log.info("Logout request received");
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/tenant")
    @Operation(summary = "Assign tenantId to current user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AuthResponse> assignTenant(
            @Valid @RequestBody AssignTenantRequest request,
            Authentication authentication) {
        AuthResponse response = authService.assignTenantToCurrentUser(
                authentication.getName(), request.tenantId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change logged-in user password")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/profile")
    @Operation(summary = "Update logged-in user profile data")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AuthResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        AuthResponse response = authService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/account")
    @Operation(summary = "Delete logged-in user account")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        authService.deleteAccount(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-email")
    @Operation(summary = "Check if user with given email exists and return userId")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Long> checkEmailExists(@RequestParam String email) {
        log.info("Checking user existence: {}", email);
        Long userId = authService.getUserIdByEmail(email);
        return ResponseEntity.ok(userId);
    }

    @GetMapping("/user-by-email")
    @Operation(summary = "Get user data by email")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AuthResponse> getUserByEmail(@RequestParam String email) {
        log.info("Fetching user data: {}", email);
        AuthResponse response = authService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/claim-ownership")
    @Operation(summary = "Bootstrap OWNER role for the current user (one-time)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AuthResponse> claimOwnership(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        AuthResponse response = authService.claimOwnership(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign-role")
    @Operation(summary = "Assign role to user")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<AuthResponse> assignRole(@RequestBody AssignRoleRequest request) {
        log.info("Assigning role {} to user {}", request.role(), request.userId());
        AuthResponse response = authService.assignRoleToUser(request.userId(), request.role());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign-tenant-to-user")
    @Operation(summary = "Assign tenantId to a specific user (OWNER only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> assignTenantToUser(@Valid @RequestBody AssignTenantToUserRequest request) {
        log.info("Assigning tenant {} to user {}", request.tenantId(), request.userId());
        authService.assignTenantToUser(request.userId(), request.tenantId());
        return ResponseEntity.ok().build();
    }

}
