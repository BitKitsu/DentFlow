package com.dentflow.identity.security;

import com.dentflow.identity.user.domain.User;
import com.dentflow.identity.user.infrastructure.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


/**
 * Filtr uwierzytelniania JWT wykonywany raz na żądanie HTTP.
 *
 * <p>Dla każdego żądania zawierającego nagłówek
 * {@code Authorization: Bearer <token>} filtr:</p>
 * <ol>
 *   <li>Wyciąga token z nagłówka.</li>
 *   <li>Odczytuje e-mail użytkownika z claima {@code sub}.</li>
 *   <li>Weryfikuje token przez {@link JwtService#isTokenValid}.</li>
 *   <li>Ładuje użytkownika z {@link UserRepository} i mapuje jego role
 *       na {@link SimpleGrantedAuthority} w formacie {@code ROLE_<NAZWA>}.</li>
 *   <li>Ustawia {@link UsernamePasswordAuthenticationToken} w
 *       {@link SecurityContextHolder}, co Spring Security traktuje
 *       jako pomyślne uwierzytelnienie.</li>
 * </ol>
 *
 * <p>Żądania bez nagłówka {@code Authorization} lub z innym schematem
 * niż {@code Bearer} są przepuszczane dalej bez uwierzytelniania —
 * Spring Security zadecyduje czy endpoint wymaga autoryzacji.</p>
 *
 * <p>Wszelkie wyjątki podczas przetwarzania tokenu są przechwytywane
 * i logowane — żądanie jest wtedy przekazywane dalej bez ustawionego
 * kontekstu, co skutkuje odpowiedzią {@code 401 Unauthorized}
 * dla chronionych endpointów.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;


    /**
     * Tworzy filtr z wstrzykniętymi zależnościami.
     *
     * @param jwtService     serwis do parsowania i walidacji tokenów JWT
     * @param userRepository repozytorium użytkowników do weryfikacji istnienia konta
     */
    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * Przetwarza nagłówek {@code Authorization} i ustawia kontekst bezpieczeństwa.
     *
     * <p>Uwierzytelnienie jest ustawiane tylko gdy spełnione są wszystkie warunki:</p>
     * <ul>
     *   <li>nagłówek {@code Authorization} zaczyna się od {@code "Bearer "}</li>
     *   <li>token zawiera prawidłowy claim {@code sub} z adresem e-mail</li>
     *   <li>kontekst bezpieczeństwa nie ma jeszcze ustawionego uwierzytelnienia</li>
     *   <li>użytkownik o danym e-mailu istnieje w bazie danych</li>
     *   <li>token przechodzi walidację w {@link JwtService#isTokenValid}</li>
     * </ul>
     *
     * @param request     przychodzące żądanie HTTP
     * @param response    odpowiedź HTTP
     * @param filterChain łańcuch filtrów Spring Security
     * @throws ServletException gdy wystąpi błąd filtra
     * @throws IOException      gdy wystąpi błąd wejścia/wyjścia
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    log.warn("Nie znaleziono użytkownika z tokenu JWT - email: {}", email);
                } else if (!jwtService.isTokenValid(token, email)) {
                    log.warn("Nieprawidłowy lub wygasły token JWT dla email: {}", email);
                } else {
                    List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRole().name()))
                            .toList();

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Uwierzytelnienie JWT pomyślne - email: {}, role: {}", email, authorities);
                }
            }
        } catch (Exception e) {
            log.error("Błąd podczas przetwarzania tokenu JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
