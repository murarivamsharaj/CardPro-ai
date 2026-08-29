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
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadEventPublisher leadEventPublisher;
    private final EmailNotificationService emailNotificationService;
    private final CardServiceClient cardServiceClient;
    private final RestTemplate restTemplate = new RestTemplate(); // Added for Webhooks

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

        // 🚀 NEW: Trigger Webhook asynchronously
        triggerWebhook(lead);

        return mapToResponse(lead);
    }

    private void triggerWebhook(Lead lead) {
        CompletableFuture.runAsync(() -> {
            // TODO: Fetch this dynamically from the user's Card settings in the future.
            // For testing, paste a Discord or Slack Webhook URL here:
            String webhookUrl = "";

            if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("content", "🚀 **New Lead from CardPro AI**\n" +
                    "**Name:** " + lead.getVisitorName() + "\n" +
                    "**Email:** " + lead.getVisitorEmail() + "\n" +
                    "**Message:** " + lead.getMessage());

            try {
                restTemplate.postForEntity(webhookUrl, payload, String.class);
            } catch (Exception e) {
                log.error("Failed to trigger webhook for lead {}: {}", lead.getId(), e.getMessage());
            }
        });
    }

    public Page<LeadResponse> getLeadsByUserId(String userId, int page, int size, String search) {
        PageRequest pageRequest = PageRequest.of(page, size);

        if (userId == null || userId.isBlank()) {
            log.warn("Leads: getLeadsByUserId called with null or blank userId");
            return Page.empty(pageRequest);
        }

        List<UUID> cardIds;
        try {
            CardProfileResponse card = cardServiceClient.getMyCard(userId, internalApiKey);
            cardIds = card != null ? List.of(card.id()) : List.of();
        } catch (FeignException e) {
            if (e instanceof FeignException.Forbidden) {
                log.error("Leads: 403 Forbidden from card-service. Check if INTERNAL_API_KEY matches in both lead-service and card-service environment variables!");
            } else if (!(e instanceof FeignException.NotFound)) {
                log.warn("Leads: could not resolve cards for user {} from card-service: status={}, message={}",
                        userId, e.status(), e.getMessage());
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