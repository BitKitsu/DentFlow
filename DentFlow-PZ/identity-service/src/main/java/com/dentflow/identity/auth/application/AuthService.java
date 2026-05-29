package com.dentflow.identity.auth.application;

import com.dentflow.identity.auth.api.AuthResponse;
import com.dentflow.identity.auth.api.ChangePasswordRequest;
import com.dentflow.identity.auth.api.LoginRequest;
import com.dentflow.identity.auth.api.RegisterRequest;
import com.dentflow.identity.auth.api.UpdateProfileRequest;
import com.dentflow.identity.security.JwtService;
import com.dentflow.identity.user.domain.Role;
import com.dentflow.identity.user.domain.User;
import com.dentflow.identity.user.domain.UserRole;
import com.dentflow.identity.user.infrastructure.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Starting registration for email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration attempt with existing email: {}", request.email());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Użytkownik z tym adresem email już istnieje");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .addressStreet(request.addressStreet())
                .addressCity(request.addressCity())
                .addressZip(request.addressZip())
                .addressCountry(request.addressCountry())
                .tenantId(0L)
                .status("ACTIVE")
                .build();

        UserRole ownerRole = UserRole.builder()
                .user(user)
                .role(Role.OWNER)
                .build();
        user.getRoles().add(ownerRole);

        User saved = userRepository.save(user);
        log.info("User registered successfully - id: {}, email: {}, role: OWNER",
                saved.getId(), saved.getEmail());

        String token = jwtService.generateToken(saved);
        return toAuthResponse(token, saved);
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.error("Login failed – user not found: {}", request.email());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Nieprawidłowy email lub hasło");
                });

        if (!"ACTIVE".equals(user.getStatus())) {
            log.warn("Login attempt on inactive account - userId: {}, status: {}",
                    user.getId(), user.getStatus());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Konto jest nieaktywne");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.error("Login failed – wrong password for email: {}", request.email());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Nieprawidłowy email lub hasło");
        }

        String token = jwtService.generateToken(user);
        log.info("Login successful - userId: {}, email: {}", user.getId(), user.getEmail());
        return toAuthResponse(token, user);
    }

    // -------------------------------------------------------------------------
    // Tenant assignment
    // -------------------------------------------------------------------------

    @Transactional
    public AuthResponse assignTenantToCurrentUser(String email, Long tenantId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Użytkownik nie istnieje"));
        user.setTenantId(tenantId);
        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);
        return toAuthResponse(token, saved);
    }

    // -------------------------------------------------------------------------
    // Password change
    // -------------------------------------------------------------------------

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Użytkownik nie istnieje"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Obecne hasło jest nieprawidłowe");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", email);
    }

    // -------------------------------------------------------------------------
    // Profile update
    // -------------------------------------------------------------------------

    @Transactional
    public AuthResponse updateProfile(String currentEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Użytkownik nie istnieje"));

        if (request.firstName()     != null) user.setFirstName(request.firstName());
        if (request.lastName()      != null) user.setLastName(request.lastName());
        if (request.phone()         != null) user.setPhone(request.phone());
        if (request.addressStreet() != null) user.setAddressStreet(request.addressStreet());
        if (request.addressCity()   != null) user.setAddressCity(request.addressCity());
        if (request.addressZip()    != null) user.setAddressZip(request.addressZip());
        if (request.addressCountry()!= null) user.setAddressCountry(request.addressCountry());
        if (request.avatarUrl()     != null) user.setAvatarUrl(request.avatarUrl());

        if (request.email() != null && !request.email().equalsIgnoreCase(currentEmail)) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Podany adres e-mail jest już zajęty");
            }
            user.setEmail(request.email());
        }

        User saved = userRepository.save(user);
        log.info("Profile updated for userId: {}", saved.getId());

        String token = jwtService.generateToken(saved);
        return toAuthResponse(token, saved);
    }

    // -------------------------------------------------------------------------
    // Account deletion
    // -------------------------------------------------------------------------

    @Transactional
    public void deleteAccount(String currentEmail) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Użytkownik nie istnieje"));
        userRepository.delete(user);
        log.info("Account deleted for userId: {}", user.getId());
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private AuthResponse toAuthResponse(String token, User user) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getTenantId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAddressStreet(),
                user.getAddressCity(),
                user.getAddressZip(),
                user.getAddressCountry(),
                user.getAvatarUrl());
    }
}
