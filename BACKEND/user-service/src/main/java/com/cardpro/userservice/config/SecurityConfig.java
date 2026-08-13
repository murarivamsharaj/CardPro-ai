package com.cardpro.userservice.config;

import com.cardpro.userservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security for user-service.
 *
 * <ul>
 *   <li>Public: CORS preflight, Swagger/OpenAPI, health</li>
 *   <li>Admin-only: the legacy full CRUD surface ({@code /api/users} without
 *       the {@code /profile}, {@code /notifications}, {@code /me} suffixes)</li>
 *   <li>Authenticated: profile + notification endpoints, resolved by the JWT
 *       email claim</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger & monitoring
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // Legacy full-CRUD management endpoints are admin-only.
                        // Profile / notifications / me are matched by path suffix
                        // so they stay authenticated for the owning user.
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/{id:[0-9]+}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/{id:[0-9]+}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{id:[0-9]+}").hasRole("ADMIN")

                        // Everything else (profile, notifications, me) requires a valid JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
