package com.cardpro.card.config;

import com.cardpro.card.security.InternalApiKeyFilter;
import com.cardpro.card.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer; // 👈 1. Added Customizer import
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final InternalApiKeyFilter internalApiKeyFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // 👈 2. Added CORS to the filter chain
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public route
                        .requestMatchers("/api/v1/cards/{slug}").permitAll()

                        // Internal route: Permitted through the authorization manager because
                        // the InternalApiKeyFilter will intercept and secure it.
                        .requestMatchers("/api/v1/cards/internal/**").permitAll()

                        // Admin route
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // All other routes require authentication
                        .anyRequest().authenticated()
                )
                // Add your custom filters
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(internalApiKeyFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}