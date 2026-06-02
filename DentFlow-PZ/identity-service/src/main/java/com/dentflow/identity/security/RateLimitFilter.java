package com.dentflow.identity.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter dla endpointu /auth/login.
 * Ogranicza maksymalnie 10 prób logowania na minutę per adres IP.
 * Przy przekroczeniu limitu zwraca HTTP 429 Too Many Requests.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    /** Ścieżka chroniona przez filtr. */
    private static final String LOGIN_PATH = "/auth/login";

    /** Maksymalna liczba żądań na minutę. */
    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    /**
     * Tablica przechowująca bucket dla każdego adresu IP.
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();



    /**
     * Wykonuje logikę rate limitingu dla żądań {@code POST /auth/login}.
     *
     * <p>Dla pozostałych ścieżek i metod HTTP żądanie jest przepuszczane
     * dalej w łańcuchu bez żadnych modyfikacji.</p>
     *
     * @param request     przychodzące żądanie HTTP
     * @param response    odpowiedź HTTP
     * @param filterChain łańcuch filtrów Spring Security
     * @throws ServletException gdy wystąpi błąd filtra
     * @throws IOException      gdy wystąpi błąd zapisu odpowiedzi
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().endsWith(LOGIN_PATH)
                && "POST".equalsIgnoreCase(request.getMethod())) {

            String clientIp = getClientIp(request);
            Bucket bucket = buckets.computeIfAbsent(clientIp, this::newBucket);

            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\": \"Za dużo prób logowania. Spróbuj ponownie za minutę.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }


    /**
     * Tworzy nowy bucket dla podanego adresu IP.
     *
     * <p>Bucket jest konfigurowany z zachłannym ({@code greedy}) uzupełnianiem
     * tokenów — dozwolone jest zużycie całego limitu naraz, a następnie
     * czekanie na pełne uzupełnienie po minucie.</p>
     *
     * @param ip adres IP klienta (używany wyłącznie jako klucz w mapie)
     * @return nowy {@link Bucket} z limitem {@value #MAX_REQUESTS_PER_MINUTE} żądań/minutę
     */
    private Bucket newBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(
                MAX_REQUESTS_PER_MINUTE,
                Refill.greedy(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Odczytuje rzeczywisty adres IP klienta z żądania HTTP.
     *
     * <p>Gdy aplikacja działa za proxy lub load balancerem, prawdziwy IP
     * klienta jest przekazywany w nagłówku {@code X-Forwarded-For}.
     * Nagłówek może zawierać listę adresów rozdzielonych przecinkami
     * (kolejne proxy) — pobierany jest zawsze pierwszy element,
     * który odpowiada oryginalnej maszynie klienta.</p>
     *
     * <p>Jeśli nagłówek jest nieobecny lub pusty, używany jest
     * {@code RemoteAddr} z samego żądania.</p>
     *
     * @param request żądanie HTTP
     * @return adres IP klienta jako {@code String}
     */
    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
