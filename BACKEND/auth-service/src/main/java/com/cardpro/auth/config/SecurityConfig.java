package com.cardpro.auth.config;

import com.cardpro.auth.security.InternalApiKeyFilter;
import com.cardpro.auth.security.JwtAuthenticationFilter;
import com.cardpro.auth.security.JwtAuthenticationProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.List;

/**
 * Spring Security 6 configuration for the auth-service.
 *
 * <p><b>Security Architecture:</b>
 * <pre>
 * Request → CORS → CorrelationIdFilter → InternalApiKeyFilter → JwtAuthenticationFilter → Actuator Endpoints
 *                                                            ↓
 *                                              AuthenticationManager
 *                                                   ↓
 *                                     JwtAuthenticationProvider
 *                                                   ↓
 *                                          UserDetailsService
 * </pre>
 *
 * <p><b>Route Security Matrix:</b>
 * <ul>
 *   <li>{@code POST /api/v1/auth/register} — PUBLIC (rate limited by gateway)</li>
 *   <li>{@code POST /api/v1/auth/login} — PUBLIC (rate limited by gateway)</li>
 *   <li>{@code POST /api/v1/auth/refresh} — PUBLIC</li>
 *   <li>{@code POST /api/v1/auth/logout} — AUTHENTICATED (JWT required)</li>
 *   <li>{@code /api/v1/auth/internal/**} — INTERNAL (X-Internal-API-Key header)</li>
 *   <li>{@code /actuator/**} — AUTHENTICATED (JWT required)</li>
 *   <li>{@code /**} (all other) — AUTHENTICATED</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ──────────────────────────────────────────────
    // Security Filter Chain
    // ──────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            @Lazy JwtAuthenticationFilter jwtAuthenticationFilter,
            @Lazy InternalApiKeyFilter internalApiKeyFilter,
            @Lazy JwtAuthenticationProvider jwtAuthenticationProvider) throws Exception {
        http
                // ── CORS ──
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ── CSRF: disabled for stateless JWT auth ──
                .csrf(AbstractHttpConfigurer::disable)

                // ── Session: stateless ──
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── Register JwtAuthenticationProvider with the AuthenticationManager ──
                .authenticationProvider(jwtAuthenticationProvider)

                // ── Exception handling ──
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))

                // ── Route authorization ──
                // ── Route authorization ──
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()

                        // ── ADD THESE 3 LINES FOR SWAGGER UI ──
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()

                        // Internal service endpoints (validated by InternalApiKeyFilter)
                        .requestMatchers("/api/v1/auth/internal/**").hasRole("INTERNAL_SERVICE")

                        // Actuator health checks
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // ── Filter ordering ──
                // Anchor both custom filters to built-in Spring Security filter positions
                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ──────────────────────────────────────────────
    // Authentication
    // ──────────────────────────────────────────────

    /**
     * Registers the JWT AuthenticationProvider with the AuthenticationManager.
     * Only JwtAuthenticationProvider is registered — username/password auth
     * is handled explicitly in AuthService during login, not via a filter.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Password encoder using BCrypt with strength factor 10 (default).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    // ──────────────────────────────────────────────
    // CORS
    // ──────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));  // Allow all origins (restricted at gateway)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Internal-API-Key",
                "X-User-Id", "X-Correlation-Id"));
        config.setExposedHeaders(List.of("Authorization", "X-Correlation-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ──────────────────────────────────────────────
    // Exception Handlers
    // ──────────────────────────────────────────────

    /**
     * Handles 401 Unauthorized — when no valid JWT is provided.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    "{\"status\":\"error\",\"error\":{\"code\":\"UNAUTHORIZED\"," +
                            "\"message\":\"Authentication is required to access this resource\"}," +
                            "\"timestamp\":\"" + Instant.now() + "\"}"
            );
        };
    }

    /**
     * Handles 403 Forbidden — when the user lacks the required role.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                    "{\"status\":\"error\",\"error\":{\"code\":\"FORBIDDEN\"," +
                            "\"message\":\"You do not have permission to access this resource\"}," +
                            "\"timestamp\":\"" + Instant.now() + "\"}"
            );
        };
    }
}