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

import java.time.Instant;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            @Lazy JwtAuthenticationFilter jwtAuthenticationFilter,
            @Lazy InternalApiKeyFilter internalApiKeyFilter,
            @Lazy JwtAuthenticationProvider jwtAuthenticationProvider) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(jwtAuthenticationProvider)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/auth/internal/**").hasRole("INTERNAL_SERVICE")
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

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