package com.cardpro.lead.config;

import com.cardpro.lead.security.InternalApiKeyFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

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
                        // CORS preflight — answered by the rawCorsFilter below, but keep
                        // the OPTIONS matcher permissive as a safety net for direct calls.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 👇 ADD THIS: Allow public access to actuator health probes for UptimeRobot
                        .requestMatchers("/actuator/**").permitAll()

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

    /**
     * Raw CORS filter mirroring card-service: answers preflight and explicitly
     * allows DELETE, PUT and PATCH so the browser can reach lead-service
     * directly (it is port-exposed in docker-compose) as well as via the
     * gateway, without CORS 403s on mutating requests.
     */
    @Bean
    public FilterRegistrationBean<Filter> rawCorsFilter() {
        Filter filter = new Filter() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
                HttpServletResponse response = (HttpServletResponse) res;
                HttpServletRequest request = (HttpServletRequest) req;

                response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");

                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                }

                chain.doFilter(req, res);
            }
        };

        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}