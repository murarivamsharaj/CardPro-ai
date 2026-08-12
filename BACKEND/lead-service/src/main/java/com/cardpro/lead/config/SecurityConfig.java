package com.cardpro.lead.config;

import com.cardpro.lead.security.InternalApiKeyFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final InternalApiKeyFilter internalApiKeyFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Stateless REST API — explicitly disable CSRF so unauthenticated
            // POSTs (e.g. public lead capture) are not rejected with 403.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Never secure the ERROR dispatch: an exception inside a
                // controller forwards to /error, and if that dispatch requires
                // authentication the client sees a misleading 403 instead of
                // the real 4xx/5xx status.
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                // Dashboard lead list — JWT is enforced upstream at the gateway,
                // so the lead-service itself must not require auth here.
                .requestMatchers(HttpMethod.GET, "/api/v1/leads").permitAll()
                // Public lead capture: unauthenticated visitors submit the
                // "Contact Me" form to POST /api/v1/leads (auth enforced at the
                // gateway). The visitor is anonymous — no X-User-Id required.
                .requestMatchers(HttpMethod.POST, "/api/v1/leads").permitAll()
                .requestMatchers("/api/v1/leads/internal/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterAfter(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
