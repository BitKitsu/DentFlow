package com.dentflow.identity.auth.application;

import com.dentflow.identity.auth.api.AuthResponse;
import com.dentflow.identity.auth.api.LoginRequest;
import com.dentflow.identity.auth.api.RegisterRequest;
import com.dentflow.identity.security.JwtService;
import com.dentflow.identity.user.domain.Role;
import com.dentflow.identity.user.domain.User;
import com.dentflow.identity.user.domain.UserRole;
import com.dentflow.identity.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashed_password")
                .tenantId(10L)
                .status("ACTIVE")
                .build();
        UserRole activeRole = UserRole.builder().user(activeUser).role(Role.OWNER).build();
        activeUser.getRoles().add(activeRole);

        inactiveUser = User.builder()
                .id(2L)
                .email("inactive@example.com")
                .passwordHash("hashed_password")
                .tenantId(10L)
                .status("INACTIVE")
                .build();
    }

    @Test
    void shouldRegisterOwnerSuccessfully() {
        // given
        RegisterRequest request = new RegisterRequest("new@example.com", "password123");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_new_password");
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(3L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("mocked_jwt_token");

        // when
        AuthResponse response = authService.register(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mocked_jwt_token");
        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.tenantId()).isEqualTo(0L); // By default during registration
        
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void shouldThrowConflictWhenEmailExistsDuringRegistration() {
        // given
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("już istnieje");
                
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken(activeUser)).thenReturn("mocked_jwt_token");

        // when
        AuthResponse response = authService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mocked_jwt_token");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.tenantId()).isEqualTo(10L);
    }

    @Test
    void shouldThrowUnauthorizedWhenEmailIncorrect() {
        // given
        LoginRequest request = new LoginRequest("wrong@example.com", "password123");
        when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Nieprawidłowy email lub hasło");
    }

    @Test
    void shouldThrowUnauthorizedWhenPasswordIncorrect() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "wrong_password");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Nieprawidłowy email lub hasło");
    }

    @Test
    void shouldThrowForbiddenWhenAccountIsInactive() {
        // given
        LoginRequest request = new LoginRequest("inactive@example.com", "password123");
        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactiveUser));

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Konto jest nieaktywne");
    }
}
