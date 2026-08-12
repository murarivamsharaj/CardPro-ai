package com.cardpro.card.config;

import com.cardpro.card.controller.InternalCardController;
import com.cardpro.card.dto.response.CardResponse;
import com.cardpro.card.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression guards for inter-service (Feign) access to the internal card
 * endpoints: GET /api/v1/cards/internal/{id} is called by lead-service
 * without any JWT (only the X-Internal-API-Key header), so the security
 * chain must permit it. The permitAll rule must be evaluated BEFORE the
 * generic GET /api/v1/cards/** authenticated rule — otherwise the internal
 * Feign call is matched first and rejected with 403 Forbidden.
 */
@WebMvcTest(
        controllers = InternalCardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {RedisConfig.class, RedisCacheConfig.class}
        )
)
@Import(SecurityConfig.class)
class InternalCardSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Test
    void internalCardLookupIsReachableWithoutAuthentication() throws Exception {
        UUID profileId = UUID.randomUUID();
        when(cardService.getCardById(profileId)).thenReturn(
                CardResponse.builder()
                        .id(profileId)
                        .slug("owner-card")
                        .templateId("basic")
                        .profileData("{\"email\":\"owner@example.com\"}")
                        .build()
        );

        mockMvc.perform(get("/api/v1/cards/internal/{profileId}", profileId))
                .andExpect(status().isOk());
    }

    @Test
    void internalIncrementViewIsReachableWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/cards/internal/{profileId}/increment-view", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    void internalMeResolvesCardsFromXUserIdHeader() throws Exception {
        UUID userId = UUID.randomUUID();
        when(cardService.getCardByUserId(userId.toString())).thenReturn(
                CardResponse.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .slug("owner-card")
                        .build()
        );

        mockMvc.perform(get("/api/v1/cards/internal/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void cardOwnerEndpointStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/cards/me"))
                .andExpect(status().isForbidden());
    }
}
