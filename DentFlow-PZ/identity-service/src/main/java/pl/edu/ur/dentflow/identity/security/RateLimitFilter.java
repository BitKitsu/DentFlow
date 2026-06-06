package pl.edu.ur.dentflow.identity.security;

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

    private static final String LOGIN_PATH = "/auth/login";
    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    // Przechowujemy osobny bucket per IP
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

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

    private Bucket newBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(
                MAX_REQUESTS_PER_MINUTE,
                Refill.greedy(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
