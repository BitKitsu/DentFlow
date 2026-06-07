package pl.edu.ur.dentflow.core.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT filter that verifies authorization tokens in HTTP requests.
 *
 * <p>Filtering occurs at the request level (OncePerRequestFilter).
 * The token is retrieved from the {@code Authorization: Bearer <token>} header.</p>
 *
 * <p>Data extracted from the token:
 * <ul>
 *   <li>email (subject) - user identifier</li>
 *   <li>userId (claim) - user ID in database</li>
 *   <li>roles (claim) - list of user roles</li>
 * </ul>
 *
 * <p>The created Authentication is saved in SecurityContext as
 * UsernamePasswordAuthenticationToken with authorities in ROLE_{role} format.</p>
 *
 * @see pl.edu.ur.dentflow.core.security.JwtService
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(token) &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                Claims claims = jwtService.extractAllClaims(token);
                String email = claims.getSubject();
                Long userId = claims.get("userId", Long.class);

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = roles == null
                        ? List.of()
                        : roles.stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                                .toList();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(email, userId, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception ignored) {
            // invalid token - request will proceed as anonymous
        }

        filterChain.doFilter(request, response);
    }
}
