package com.cardpro.discovery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Actuator probes (health/info/metrics) must be reachable without
                // credentials so Docker/K8s healthchecks and service_healthy gates work.
                .requestMatchers("/actuator/health/**", "/actuator/info", "/actuator/metrics/**").permitAll()
                // Eureka dashboard and registration endpoints stay Basic-auth protected
                // (clients authenticate via http://eureka:eureka123@localhost:8761/eureka/).
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {});
        return http.build();
    }
}
