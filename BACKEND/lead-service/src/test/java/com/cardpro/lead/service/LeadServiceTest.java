package com.cardpro.lead.service;

import com.cardpro.lead.client.CardProfileResponse;
import com.cardpro.lead.client.CardServiceClient;
import com.cardpro.lead.dto.response.LeadResponse;
import com.cardpro.lead.entity.Lead;
import com.cardpro.lead.repository.LeadRepository;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression guards for the "My Leads" dashboard flow: GET /api/v1/leads must
 * resolve the logged-in user's cards from card-service (via the X-User-Id
 * header) and return the leads captured against those card ids — not a hard
 * coded empty page, which is what made the dashboard show "No leads yet" even
 * when leads existed in the database.
 */
@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    private static final String INTERNAL_API_KEY = "test-internal-key";

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private LeadEventPublisher leadEventPublisher;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private CardServiceClient cardServiceClient;

    private LeadService leadService;

    @BeforeEach
    void setUp() {
        leadService = new LeadService(leadRepository, leadEventPublisher, emailNotificationService, cardServiceClient);
        ReflectionTestUtils.setField(leadService, "internalApiKey", INTERNAL_API_KEY);
    }

    @Test
    void getLeadsByUserId_returnsLeadsCapturedAgainstUsersCards() {
        String userId = UUID.randomUUID().toString();
        UUID cardId = UUID.randomUUID();
        when(cardServiceClient.getMyCard(userId, INTERNAL_API_KEY))
                .thenReturn(new CardProfileResponse(cardId, UUID.randomUUID(), "owner-card",
                        "basic", "{}", null, true));

        Lead lead = Lead.builder()
                .id(UUID.randomUUID())
                .profileId(cardId)
                .visitorName("Jane Doe")
                .visitorEmail("jane@example.com")
                .visitorPhone("555-0100")
                .message("Interested in your services")
                .capturedAt(LocalDateTime.now())
                .build();

        when(leadRepository.findByProfileIdInOrderByCapturedAtDesc(List.of(cardId), org.springframework.data.domain.PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(lead)));

        Page<LeadResponse> result = leadService.getLeadsByUserId(userId, 0, 20, "");

        assertThat(result.getContent()).hasSize(1);
        LeadResponse response = result.getContent().get(0);
        assertThat(response.getId()).isEqualTo(lead.getId());
        assertThat(response.getProfileId()).isEqualTo(cardId);
        assertThat(response.getVisitorName()).isEqualTo("Jane Doe");
        assertThat(response.getVisitorEmail()).isEqualTo("jane@example.com");

        // The user id from the X-User-Id header is forwarded to card-service
        // unchanged, together with the internal API key.
        verify(cardServiceClient).getMyCard(userId, INTERNAL_API_KEY);
    }

    @Test
    void getLeadsByUserId_returnsEmptyPageWhenUserHasNoCard() {
        String userId = UUID.randomUUID().toString();
        Request request = Request.create(Request.HttpMethod.GET,
                "http://card-service/api/v1/cards/internal/me", Map.of(), new byte[0], StandardCharsets.UTF_8);
        when(cardServiceClient.getMyCard(userId, INTERNAL_API_KEY))
                .thenThrow(new FeignException.NotFound("Card profile not found", request, new byte[0], Map.of()));

        Page<LeadResponse> result = leadService.getLeadsByUserId(userId, 0, 20, "");

        assertThat(result.getContent()).isEmpty();
        // No card -> no point querying the lead repository.
        verify(leadRepository, never()).findByProfileIdInOrderByCapturedAtDesc(any(), any());
    }

    @Test
    void getLeadsByUserId_returnsEmptyPageWhenCardLookupReturnsNull() {
        String userId = UUID.randomUUID().toString();
        when(cardServiceClient.getMyCard(userId, INTERNAL_API_KEY)).thenReturn(null);

        Page<LeadResponse> result = leadService.getLeadsByUserId(userId, 0, 20, "");

        assertThat(result.getContent()).isEmpty();
        verify(leadRepository, never()).findByProfileIdInOrderByCapturedAtDesc(any(), any());
    }
}
