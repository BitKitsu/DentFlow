package com.dentflow.identity.auth.application;

import com.dentflow.identity.auth.api.AuthResponse;
import com.dentflow.identity.auth.api.LoginRequest;
import com.dentflow.identity.auth.api.RegisterRequest;
import com.dentflow.identity.security.JwtService;
import com.dentflow.identity.user.domain.Role;
import com.dentflow.identity.user.domain.User;
import com.dentflow.identity.user.domain.UserRole;
import com.dentflow.identity.user.infrastructure.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Rejestracja gabinetu - tworzy konto OWNER.
     * tenantId jest tymczasowo ustawiane na 0; core-service powinien
     * zaktualizować tenantId po stworzeniu gabinetu (lub wywoływać ten endpoint
     * z podanym tenantId w przyszłości).
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Użytkownik z tym adresem email już istnieje");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .tenantId(0L) // zostanie zaktualizowane po rejestracji gabinetu w core-service
                .status("ACTIVE")
                .build();

        UserRole ownerRole = UserRole.builder()
                .user(user)
                .role(Role.OWNER)
                .build();
        user.getRoles().add(ownerRole);

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        return new AuthResponse(token, saved.getId(), saved.getEmail(), saved.getTenantId());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Nieprawidłowy email lub hasło"));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Konto jest nieaktywne");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Nieprawidłowy email lub hasło");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getTenantId());
    }
    @Transactional
    public AuthResponse assignTenantToCurrentUser(String email, Long tenantId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Użytkownik nie istnieje"));
        user.setTenantId(tenantId);
        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);
        return new AuthResponse(token, saved.getId(), saved.getEmail(), saved.getTenantId());
}
}
