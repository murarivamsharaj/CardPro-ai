package com.cardpro.card.controller;

import com.cardpro.card.config.RedisCacheConfig;
import com.cardpro.card.config.RedisConfig;
import com.cardpro.card.config.SecurityConfig;
import com.cardpro.card.dto.request.AnalyticsEventRequest;
import com.cardpro.card.dto.response.AdminAnalyticsResponse;
import com.cardpro.card.dto.response.AnalyticsResponse;
import com.cardpro.card.service.AnalyticsService;
import com.cardpro.card.service.CardService;
import io.jsonwebtoken.Jwts;
import org.springframework.http.MediaType;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security + payload guards for the analytics endpoints:
 * <ul>
 *   <li>{@code GET /summary} requires a valid JWT and returns the caller's
 *       own metrics (identity from the token, not a spoofable header)</li>
 *   <li>{@code GET /admin/total-cards} and {@code GET /admin/overview} are
 *       ADMIN-only — a plain USER token must get 403, not data</li>
 * </ul>
 */
@WebMvcTest(
        controllers = AnalyticsController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {RedisConfig.class, RedisCacheConfig.class}
        )
)
@Import(SecurityConfig.class)
class AnalyticsControllerTest {

    private static final String JWT_SECRET = "CardProSuperSecretKeyThatIsLongEnoughForHS2561234567890";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private CardService cardService;

    private String tokenFor(String userId, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim("email", "test@example.com")
                .claim("roles", List.of(role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    void summaryRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    void trackEventIsPublicAndForwardsToService() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\":\"" + UUID.randomUUID()
                                + "\",\"eventType\":\"PAGE_VIEW\",\"visitorId\":\"visitor-1\"}"))
                .andExpect(status().isOk());

        verify(cardService).trackEvent(any(AnalyticsEventRequest.class));
    }

    @Test
    void trackEventRejectsPayloadMissingProfileId() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventType\":\"PAGE_VIEW\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void summaryReturnsOwnMetricsForValidJwt() throws Exception {
        when(analyticsService.getAnalyticsForUser(anyString(), anyInt())).thenReturn(
                AnalyticsResponse.builder()
                        .totalViews(42)
                        .uniqueVisitors(17)
                        .totalLeads(3)
                        .clickThroughRate(4.5)
                        .viewsByDate(Map.of("2026-08-15", 10L))
                        .clicksByLink(Map.of("LinkedIn", 2L))
                        .build()
        );

        mockMvc.perform(get("/api/v1/analytics/summary")
                        .header("Authorization", "Bearer " + tokenFor("user-123", "ROLE_USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalViews").value(42))
                .andExpect(jsonPath("$.uniqueVisitors").value(17))
                .andExpect(jsonPath("$.totalLeads").value(3))
                .andExpect(jsonPath("$.clickThroughRate").value(4.5))
                .andExpect(jsonPath("$.viewsByDate['2026-08-15']").value(10))
                .andExpect(jsonPath("$.clicksByLink['LinkedIn']").value(2));
    }

    @Test
    void totalCardsIsAdminOnly() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/admin/total-cards")
                        .header("Authorization", "Bearer " + tokenFor("user-123", "ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void totalCardsAllowedForAdmin() throws Exception {
        when(analyticsService.getTotalCardCount()).thenReturn(7L);

        mockMvc.perform(get("/api/v1/analytics/admin/total-cards")
                        .header("Authorization", "Bearer " + tokenFor("admin-1", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCards").value(7));
    }

    @Test
    void adminOverviewAllowedForAdmin() throws Exception {
        when(analyticsService.getAdminOverview())
                .thenReturn(new AdminAnalyticsResponse(10, 8, 120, 5));

        mockMvc.perform(get("/api/v1/analytics/admin/overview")
                        .header("Authorization", "Bearer " + tokenFor("admin-1", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCards").value(10))
                .andExpect(jsonPath("$.activeCards").value(8))
                .andExpect(jsonPath("$.totalViews").value(120))
                .andExpect(jsonPath("$.viewsLast7Days").value(5));
    }

    @Test
    void adminOverviewRejectedForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/admin/overview")
                        .header("Authorization", "Bearer " + tokenFor("user-123", "ROLE_USER")))
                .andExpect(status().isForbidden());
    }
}
