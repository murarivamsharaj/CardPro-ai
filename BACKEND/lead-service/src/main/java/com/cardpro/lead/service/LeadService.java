package com.cardpro.lead.service;

import com.cardpro.lead.client.CardProfileResponse;
import com.cardpro.lead.client.CardServiceClient;
import com.cardpro.lead.dto.request.SubmitLeadRequest;
import com.cardpro.lead.dto.response.LeadResponse;
import com.cardpro.lead.entity.Lead;
import com.cardpro.lead.repository.LeadRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadEventPublisher leadEventPublisher;
    private final EmailNotificationService emailNotificationService;
    private final CardServiceClient cardServiceClient;

    @Value("${app.internal.api-key}")
    private String internalApiKey;

    public LeadResponse submitLead(SubmitLeadRequest request) {
        Lead lead = Lead.builder()
            .profileId(request.getProfileId())
            .visitorName(request.getVisitorName())
            .visitorEmail(request.getVisitorEmail())
            .visitorPhone(request.getVisitorPhone())
            .message(request.getMessage())
            .build();

        lead = leadRepository.save(lead);

        // Notify the card owner asynchronously without blocking the HTTP thread
        emailNotificationService.sendNewLeadEmail(lead);

        // Publish async event for AI follow-up generation
        leadEventPublisher.publishLeadCreated(lead);

        return mapToResponse(lead);
    }

    public Page<LeadResponse> getLeadsByUserId(String userId, int page, int size, String search) {
        PageRequest pageRequest = PageRequest.of(page, size);

        // Resolve the user's card ids via card-service. A user with no card yet
        // gets a 404 from the internal endpoint — treat that as "no leads" rather
        // than surfacing an error on the dashboard. Any other card-service
        // failure (500, timeouts, discovery hiccups) degrades the same way: the
        // dashboard must never hard-fail just because the card lookup hiccuped.
        List<UUID> cardIds;
        try {
            CardProfileResponse card = cardServiceClient.getMyCard(userId, internalApiKey);
            cardIds = card != null ? List.of(card.id()) : List.of();
        } catch (FeignException e) {
            if (!(e instanceof FeignException.NotFound)) {
                log.warn("Leads: could not resolve cards for user {} from card-service: {}",
                        userId, e.getMessage());
            }
            return Page.empty(pageRequest);
        }

        if (cardIds.isEmpty()) {
            return Page.empty(pageRequest);
        }

        String keyword = search == null ? "" : search.trim();
        if (keyword.isEmpty()) {
            return leadRepository.findByProfileIdInOrderByCapturedAtDesc(cardIds, pageRequest)
                .map(this::mapToResponse);
        }

        return leadRepository.searchByProfileIds(cardIds, keyword, pageRequest)
            .map(this::mapToResponse);
    }

    public String getFollowup(String leadId) {
        Lead lead = leadRepository.findById(UUID.fromString(leadId))
            .orElseThrow(() -> new RuntimeException("Lead not found"));
        return lead.getAiFollowup() != null ? lead.getAiFollowup() : "No follow-up generated yet.";
    }

    public void deductCredit(String userId) {
        // Integrate with auth-service for credit deduction
    }

    /**
     * Number of leads captured against a single card profile. Used by
     * card-service's analytics aggregation (via {@code GET /internal/count}).
     */
    public long countLeadsForProfile(UUID profileId) {
        return leadRepository.countByProfileId(profileId);
    }

    private LeadResponse mapToResponse(Lead lead) {
        return LeadResponse.builder()
            .id(lead.getId())
            .profileId(lead.getProfileId())
            .visitorName(lead.getVisitorName())
            .visitorEmail(lead.getVisitorEmail())
            .visitorPhone(lead.getVisitorPhone())
            .message(lead.getMessage())
            .aiFollowup(lead.getAiFollowup())
            .capturedAt(lead.getCapturedAt())
            .build();
    }
}
