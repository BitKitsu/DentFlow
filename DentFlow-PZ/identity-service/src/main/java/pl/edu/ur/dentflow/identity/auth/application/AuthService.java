package pl.edu.ur.dentflow.identity.auth.application;

import pl.edu.ur.dentflow.identity.auth.api.AuthResponse;
import pl.edu.ur.dentflow.identity.auth.api.ChangePasswordRequest;
import pl.edu.ur.dentflow.identity.auth.api.LoginRequest;
import pl.edu.ur.dentflow.identity.auth.api.RegisterRequest;
import pl.edu.ur.dentflow.identity.auth.api.UpdateProfileRequest;
import pl.edu.ur.dentflow.identity.security.JwtService;
import pl.edu.ur.dentflow.identity.user.domain.Role;
import pl.edu.ur.dentflow.identity.user.domain.User;
import pl.edu.ur.dentflow.identity.user.domain.UserRole;
import pl.edu.ur.dentflow.identity.user.infrastructure.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service responsible for user authentication and authorization.
 * Handles registration, login, role assignment, profile management,
 * and JWT token generation.
 *
 * <p>Business logic:
 * <ul>
 *   <li>Registration creates an account with PATIENT role and tenantId=0 (clinic assignment is separate)</li>
 *   <li>Login verifies account status, password (BCrypt) and generates JWT token</li>
 *   <li>Password is validated before persistence by PasswordEncoder</li>
 * </ul>
 *
 * @see pl.edu.ur.dentflow.identity.security.JwtService
 * @see pl.edu.ur.dentflow.identity.user.infrastructure.UserRepository
 */
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
                    "A user with this email already exists");
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

        UserRole patientRole = UserRole.builder()
                .user(user)
                .role(Role.PATIENT)
                .build();
        user.getRoles().add(patientRole);

        User saved = userRepository.save(user);
        log.info("User registered successfully - id: {}, email: {}, role: PATIENT",
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
                            "Invalid email or password");
                });

        if (!"ACTIVE".equals(user.getStatus())) {
            log.warn("Login attempt on inactive account - userId: {}, status: {}",
                    user.getId(), user.getStatus());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.error("Login failed – wrong password for email: {}", request.email());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
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
                        "User does not exist"));
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
                        "User does not exist"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
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
                        "User does not exist"));

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
                        "The provided email address is already taken");
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
                        "User does not exist"));
        userRepository.delete(user);
        log.info("Account deleted for userId: {}", user.getId());
    }

    // -------------------------------------------------------------------------
    // User lookup by email
    // -------------------------------------------------------------------------

    public Long getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User does not exist"));
        return user.getId();
    }

    public AuthResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User does not exist"));
        String token = jwtService.generateToken(user);
        return toAuthResponse(token, user);
    }

    // -------------------------------------------------------------------------
    // Owner bootstrap
    // -------------------------------------------------------------------------

    @Transactional
    public AuthResponse claimOwnership(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User does not exist"));

        boolean alreadyOwner = user.getRoles().stream()
                .anyMatch(ur -> ur.getRole() == Role.OWNER);

        if (alreadyOwner) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "User is already an owner");
        }

        UserRole ownerRole = UserRole.builder()
                .user(user)
                .role(Role.OWNER)
                .build();
        user.getRoles().add(ownerRole);
        User saved = userRepository.save(user);
        log.info("Owner claim successful for userId: {}", saved.getId());

        String token = jwtService.generateToken(saved);
        return toAuthResponse(token, saved);
    }

    // -------------------------------------------------------------------------
    // Role assignment
    // -------------------------------------------------------------------------

    @Transactional
    public AuthResponse assignRoleToUser(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User does not exist"));
        
        // Check if user already has this role
        boolean hasRole = user.getRoles().stream()
                .anyMatch(ur -> ur.getRole() == role);
        
        if (!hasRole) {
            UserRole userRole = UserRole.builder()
                    .user(user)
                    .role(role)
                    .build();
            user.getRoles().add(userRole);
            userRepository.save(user);
            log.info("Assigned role {} to userId: {}", role, userId);
        } else {
            log.info("User {} already has role {}", userId, role);
        }

        String token = jwtService.generateToken(user);
        return toAuthResponse(token, user);
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
