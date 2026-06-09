package pl.edu.ur.dentflow.identity.auth.application;

import pl.edu.ur.dentflow.identity.auth.api.AuthResponse;
import pl.edu.ur.dentflow.identity.auth.api.LoginRequest;
import pl.edu.ur.dentflow.identity.auth.api.RegisterRequest;
import pl.edu.ur.dentflow.identity.auth.api.ChangePasswordRequest;
import pl.edu.ur.dentflow.identity.user.domain.User;
import pl.edu.ur.dentflow.identity.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.web.server.ResponseStatusException;

/**
 * Integration tests for the authorization flow (auth flow).
 * Verifies the complete cycle: registration → login → password change → profile.
 *
 * <p>Tests run the full Spring Boot context with H2 in-memory database.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:identity_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:schema.sql",
    "spring.flyway.enabled=false"
})
@Transactional
class AuthFlowIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCompleteFullAuthFlow() {
        // 1. Register a new user
        RegisterRequest registerRequest = new RegisterRequest(
                "integration@test.com", "password123", "Jan", "Testowy",
                "+48 123 456 789", null, null, null, null);
        AuthResponse registered = authService.register(registerRequest);

        assertThat(registered).isNotNull();
        assertThat(registered.token()).isNotBlank();
        assertThat(registered.email()).isEqualTo("integration@test.com");
        assertThat(registered.tenantId()).isEqualTo(0L);

        // 2. Login with registered user
        LoginRequest loginRequest = new LoginRequest("integration@test.com", "password123");
        AuthResponse logged = authService.login(loginRequest);

        assertThat(logged).isNotNull();
        assertThat(logged.token()).isNotBlank();
        assertThat(logged.email()).isEqualTo("integration@test.com");
        assertThat(logged.userId()).isEqualTo(registered.userId());

        // 3. Change password
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("password123", "newPassword456");
        authService.changePassword("integration@test.com", changePasswordRequest);

        // 4. Login with new password
        LoginRequest loginWithNewPassword = new LoginRequest("integration@test.com", "newPassword456");
        AuthResponse loggedWithNewPassword = authService.login(loginWithNewPassword);

        assertThat(loggedWithNewPassword).isNotNull();
        assertThat(loggedWithNewPassword.token()).isNotBlank();

        // 5. Reject login with old password
        LoginRequest loginWithOldPassword = new LoginRequest("integration@test.com", "password123");
        assertThatThrownBy(() -> authService.login(loginWithOldPassword))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldRejectRegistrationWithDuplicateEmail() {
        // 1. First registration
        RegisterRequest request1 = new RegisterRequest(
                "duplicate@test.com", "password123", null, null, null, null, null, null, null);
        authService.register(request1);

        // 2. Attempt registration with same email
        RegisterRequest request2 = new RegisterRequest(
                "duplicate@test.com", "password456", null, null, null, null, null, null, null);
        assertThatThrownBy(() -> authService.register(request2))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(409);
                });
    }

    @Test
    void shouldRejectLoginWithWrongPassword() {
        // 1. Register user
        RegisterRequest registerRequest = new RegisterRequest(
                "wrongpass@test.com", "correctPassword", null, null, null, null, null, null, null);
        authService.register(registerRequest);

        // 2. Login with wrong password
        LoginRequest loginRequest = new LoginRequest("wrongpass@test.com", "wrongPassword");
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(401);
                });
    }

    @Test
    void shouldRejectLoginForNonexistentUser() {
        LoginRequest loginRequest = new LoginRequest("nonexistent@test.com", "password123");
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(401);
                });
    }

    @Test
    void shouldAssignRoleToUser() {
        // 1. Register user
        RegisterRequest registerRequest = new RegisterRequest(
                "roleassign@test.com", "password123", null, null, null, null, null, null, null);
        AuthResponse registered = authService.register(registerRequest);

        // 2. Assign role
        AuthResponse withRole = authService.assignRoleToUser(
                registered.userId(), pl.edu.ur.dentflow.identity.user.domain.Role.DENTIST);

        assertThat(withRole).isNotNull();

        // 3. Verify role was assigned
        User user = userRepository.findById(registered.userId()).orElseThrow();
        assertThat(user.getRoles()).anyMatch(ur -> ur.getRole() == pl.edu.ur.dentflow.identity.user.domain.Role.DENTIST);
    }

    @Test
    void shouldDeleteUserAccount() {
        // 1. Register user
        RegisterRequest registerRequest = new RegisterRequest(
                "delete@test.com", "password123", null, null, null, null, null, null, null);
        AuthResponse registered = authService.register(registerRequest);

        // 2. Delete account
        authService.deleteAccount("delete@test.com");

        // 3. Verify account was deleted
        assertThat(userRepository.findByEmail("delete@test.com")).isEmpty();
    }

    @Test
    void shouldRejectPasswordChangeWithWrongCurrentPassword() {
        // 1. Register user
        RegisterRequest registerRequest = new RegisterRequest(
                "changepw@test.com", "correctPassword", null, null, null, null, null, null, null);
        authService.register(registerRequest);

        // 2. Attempt password change with wrong current password
        ChangePasswordRequest changeRequest = new ChangePasswordRequest("wrongCurrent", "newPassword");
        assertThatThrownBy(() -> authService.changePassword("changepw@test.com", changeRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(401);
                });
    }
}
