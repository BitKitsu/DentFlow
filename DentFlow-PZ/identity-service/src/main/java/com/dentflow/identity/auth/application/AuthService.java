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

/**
 * Serwis aplikacyjny obsługujący uwierzytelnianie i zarządzanie kontem użytkownika.
 *
 * <p>Odpowiada za pełny cykl życia konta: rejestrację, logowanie,
 * zmianę hasła, aktualizację profilu oraz usunięcie konta.
 * Po każdej operacji modyfikującej dane użytkownika zwracany jest
 * świeży token JWT odzwierciedlający aktualny stan konta.</p>
 *
 * <p>Każda metoda wyszukuje użytkownika wyłącznie po adresie e-mail
 * pochodzącym z zweryfikowanego tokenu JWT — co gwarantuje że
 * użytkownik może modyfikować tylko własne konto.</p>
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Tworzy serwis z wstrzykniętymi zależnościami.
     *
     * @param userRepository  repozytorium użytkowników
     * @param passwordEncoder enkoder haseł (BCrypt)
     * @param jwtService      serwis generowania tokenów JWT
     */
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
    /**
     * Rejestruje nowego użytkownika i zwraca token JWT.
     *
     * <p>Nowo utworzone konto otrzymuje domyślnie rolę {@link Role#OWNER}
     * oraz {@code tenantId = 0} — tenant jest przypisywany osobno
     * przez {@link #assignTenantToCurrentUser}.</p>
     *
     * @param request dane rejestracyjne nowego użytkownika
     * @return {@link AuthResponse} z tokenem JWT i danymi konta
     * @throws ResponseStatusException {@code 409 Conflict} gdy e-mail jest już zajęty
     */
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

    /**
     * Uwierzytelnia użytkownika na podstawie e-maila i hasła.
     *
     * <p>Celowo zwraca ten sam komunikat błędu ({@code "Nieprawidłowy email lub hasło"})
     * zarówno gdy użytkownik nie istnieje, jak i gdy hasło jest błędne —
     * zapobiega to enumeracji kont przez atakującego.</p>
     *
     * @param request dane logowania (e-mail i hasło)
     * @return {@link AuthResponse} z tokenem JWT i danymi konta
     * @throws ResponseStatusException {@code 401 Unauthorized} gdy dane są nieprawidłowe
     * @throws ResponseStatusException {@code 403 Forbidden} gdy konto jest nieaktywne
     */
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
    /**
     * Przypisuje tenanta do konta użytkownika i zwraca odświeżony token JWT.
     *
     * <p>Wywoływana po pomyślnym utworzeniu lub dołączeniu do gabinetu —
     * nowy token zawiera zaktualizowany {@code tenantId}, dzięki czemu
     * kolejne żądania mają od razu właściwy kontekst gabinetu.</p>
     *
     * @param email    adres e-mail użytkownika z kontekstu bezpieczeństwa
     * @param tenantId identyfikator gabinetu do przypisania
     * @return {@link AuthResponse} z nowym tokenem JWT zawierającym {@code tenantId}
     * @throws ResponseStatusException {@code 404 Not Found} gdy użytkownik nie istnieje
     */
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
    /**
     * Zmienia hasło zalogowanego użytkownika.
     *
     * <p>Wymaga podania aktualnego hasła w celu weryfikacji tożsamości —
     * zapobiega zmianie hasła przez osobę która przejęła aktywną sesję.</p>
     *
     * @param email   adres e-mail użytkownika z kontekstu bezpieczeństwa
     * @param request obiekt zawierający aktualne i nowe hasło
     * @throws ResponseStatusException {@code 404 Not Found} gdy użytkownik nie istnieje
     * @throws ResponseStatusException {@code 401 Unauthorized} gdy aktualne hasło jest błędne
     */
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
    /**
     * Aktualizuje dane profilowe zalogowanego użytkownika.
     *
     * <p>Wszystkie pola żądania są opcjonalne — wartość {@code null}
     * oznacza brak zmiany danego pola (partial update).</p>
     *
     * <p>Zmiana adresu e-mail jest dozwolona pod warunkiem że nowy
     * e-mail nie jest już zajęty przez inne konto. Po zmianie e-maila
     * zwrócony token zawiera nowy adres jako claim {@code sub} —
     * klient powinien zastąpić nim dotychczasowy token.</p>
     *
     * @param currentEmail aktualny adres e-mail użytkownika z kontekstu bezpieczeństwa
     * @param request      nowe dane profilowe (wszystkie pola opcjonalne)
     * @return {@link AuthResponse} z odświeżonym tokenem JWT i zaktualizowanymi danymi
     * @throws ResponseStatusException {@code 404 Not Found} gdy użytkownik nie istnieje
     * @throws ResponseStatusException {@code 409 Conflict} gdy nowy e-mail jest już zajęty
     */
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
    /**
     * Trwale usuwa konto zalogowanego użytkownika.
     *
     * <p>Operacja jest nieodwracalna. Usunięcie kaskadowo usuwa również
     * wszystkie powiązane role ({@link UserRole}) dzięki konfiguracji
     * {@code CascadeType.ALL} + {@code orphanRemoval} na encji {@link User}.</p>
     *
     * @param currentEmail adres e-mail użytkownika z kontekstu bezpieczeństwa
     * @throws ResponseStatusException {@code 404 Not Found} gdy użytkownik nie istnieje
     */
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
    /**
     * Mapuje użytkownika i token JWT na obiekt odpowiedzi API.
     *
     * @param token wygenerowany token JWT
     * @param user  encja użytkownika
     * @return {@link AuthResponse} gotowy do zwrócenia przez kontroler
     */
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
