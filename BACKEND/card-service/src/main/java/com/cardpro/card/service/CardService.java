package com.cardpro.card.service;

import com.cardpro.card.client.UserServiceClient;
import com.cardpro.card.dto.request.AnalyticsEventRequest;
import com.cardpro.card.dto.request.CreateCardRequest;
import com.cardpro.card.dto.request.UpdateCardRequest;
import com.cardpro.card.dto.response.CardResponse;
import com.cardpro.card.dto.response.PublicCardResponse;
import com.cardpro.card.dto.response.UserWatermarkResponse;
import com.cardpro.card.exception.CardNotFoundException;
import com.cardpro.card.entity.CardEvent;
import com.cardpro.card.entity.CardEventType;
import com.cardpro.card.entity.CardProfile;
import com.cardpro.card.repository.CardEventRepository;
import com.cardpro.card.repository.CardProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardProfileRepository cardProfileRepository;
    private final CardEventRepository cardEventRepository;
    private final CardCacheService cardCacheService;
    private final SlugService slugService;
    private final ObjectMapper objectMapper;
    private final UserServiceClient userServiceClient;

    @Value("${app.internal.api-key}")
    private String internalApiKey;

    public PublicCardResponse getPublicCard(String slug) {
        // Try cache first
        PublicCardResponse cached = cardCacheService.getCachedProfile(slug);
        if (cached != null) {
            return cached;
        }

        PublicCardResponse response = findBySlug(slug);
        cardCacheService.cacheProfile(slug, response);
        return response;
    }

    /**
     * Dedicated public query: resolves a card by its unique slug and returns
     * the public details (no owner information). Deactivated cards are treated
     * as not found so an offline card's link stops resolving.
     */
    public PublicCardResponse findBySlug(String slug) {
        CardProfile profile = cardProfileRepository.findBySlug(slug)
                .orElseThrow(CardNotFoundException::new);

        if (Boolean.FALSE.equals(profile.getIsActive())) {
            throw new CardNotFoundException();
        }

        return mapToPublicResponse(profile);
    }

    public CardResponse getCardByUserId(String userId) {
        CardProfile profile = cardProfileRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(CardNotFoundException::new);
        return mapToResponse(profile);
    }

    public CardResponse getCardById(UUID profileId) {
        CardProfile profile = cardProfileRepository.findById(profileId)
                .orElseThrow(CardNotFoundException::new);
        return mapToResponse(profile);
    }

    public Page<CardResponse> getAllCards(Pageable pageable) {
        return cardProfileRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<CardResponse> searchCards(String keyword, Pageable pageable) {
        return cardProfileRepository.findBySlugContainingIgnoreCase(keyword, pageable)
                .map(this::mapToResponse);
    }

    public CardResponse createCard(String userId, String ownerEmail, CreateCardRequest request) {
        slugService.validateSlug(request.getSlug());

        CardProfile profile = CardProfile.builder()
                .userId(UUID.fromString(userId))
                .ownerEmail(ownerEmail)
                .slug(request.getSlug())
                .templateId(request.getTemplateId() != null ? request.getTemplateId() : "basic")
                .profileData(convertToJsonString(request.getProfileData()))
                .address(request.getAddress())
                .socialLinks(request.getSocialLinks() != null ? request.getSocialLinks() : new HashMap<>())
                .build();

        profile = cardProfileRepository.save(profile);
        return mapToResponse(profile);
    }

    public CardResponse updateCard(String userId, String ownerEmail, UpdateCardRequest request) {
        CardProfile profile = cardProfileRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(CardNotFoundException::new);

        if (request.getSlug() != null) {
            slugService.validateSlug(request.getSlug());
            profile.setSlug(request.getSlug());
        }
        if (request.getTemplateId() != null) {
            profile.setTemplateId(request.getTemplateId());
        }
        if (request.getProfileData() != null) {
            profile.setProfileData(convertToJsonString(request.getProfileData()));
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getSocialLinks() != null) {
            profile.setSocialLinks(request.getSocialLinks());
        }
        if (request.getIsActive() != null) {
            profile.setIsActive(request.getIsActive());
        }
        if (ownerEmail != null && !ownerEmail.isBlank()) {
            // Keep the owner's account email current — it drives the watermark
            // preference lookup on the public card render.
            profile.setOwnerEmail(ownerEmail);
        }

        profile = cardProfileRepository.save(profile);
        cardCacheService.evictCache(profile.getSlug());
        return mapToResponse(profile);
    }

    public void deleteCard(String userId) {
        CardProfile profile = cardProfileRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(CardNotFoundException::new);

        cardProfileRepository.delete(profile);
        cardCacheService.evictCache(profile.getSlug());
    }

    @Transactional
    public void incrementViewCount(UUID profileId) {
        incrementViewCount(profileId, null);
    }

    /**
     * Bumps the cumulative view counter on the profile AND appends a VIEW event
     * to the analytics log. The optional {@code visitorId} (a caller-supplied
     * session id) is what makes the unique-visitor metric meaningful.
     */
    @Transactional
    public void incrementViewCount(UUID profileId, String visitorId) {
        incrementViewCount(profileId, visitorId, CardEventType.VIEW);
    }

    /**
     * Variant used by the public events endpoint so the stored event type
     * reflects the caller ({@code PAGE_VIEW} instead of the internal
     * {@code VIEW}); both are counted as views by the analytics aggregation.
     */
    @Transactional
    public void incrementViewCount(UUID profileId, String visitorId, CardEventType eventType) {
        CardProfile profile = cardProfileRepository.findById(profileId)
                .orElseThrow(CardNotFoundException::new);

        profile.setViewCount(profile.getViewCount() + 1);
        cardProfileRepository.save(profile);
        cardEventRepository.save(CardEvent.builder()
                .profileId(profileId)
                .eventType(eventType)
                .visitorId(visitorId)
                .build());

        // Evict the cache so the updated view count shows on the next request
        cardCacheService.evictCache(profile.getSlug());
    }

    /**
     * Records a CLICK event when a visitor taps one of the card's social /
     * portfolio links. The counter column on {@code card_profiles} is untouched;
     * the analytics log is the source of truth for click metrics.
     */
    @Transactional
    public void recordClick(UUID profileId, String linkLabel, String visitorId) {
        recordClick(profileId, linkLabel, visitorId, CardEventType.CLICK);
    }

    @Transactional
    public void recordClick(UUID profileId, String linkLabel, String visitorId, CardEventType eventType) {
        // Fail with 404 (rather than a dangling event row) if the card does not exist
        cardProfileRepository.findById(profileId)
                .orElseThrow(CardNotFoundException::new);

        cardEventRepository.save(CardEvent.builder()
                .profileId(profileId)
                .eventType(eventType)
                .linkLabel(linkLabel)
                .visitorId(visitorId)
                .build());
    }

    /**
     * Ingestion entry point for the public events endpoint
     * ({@code POST /api/v1/analytics/events}). Maps the accepted event types to
     * the underlying counters/log rows:
     *
     * <ul>
     *   <li>{@code PAGE_VIEW} — bumps the cumulative view counter + logs the view</li>
     *   <li>{@code SOCIAL_CLICK} / {@code BUTTON_CLICK} — logs a click on the link</li>
     *   <li>{@code VCF_DOWNLOAD} — logs the vCard download as a click on "vCard"</li>
     * </ul>
     */
    @Transactional
    public void trackEvent(AnalyticsEventRequest request) {
        CardEventType type;
        try {
            type = CardEventType.valueOf(request.eventType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported event type: " + request.eventType());
        }

        switch (type) {
            case PAGE_VIEW, VIEW -> incrementViewCount(request.profileId(), request.visitorId(), type);
            case SOCIAL_CLICK, BUTTON_CLICK, CLICK ->
                    recordClick(request.profileId(), request.linkLabel(), request.visitorId(), type);
            case VCF_DOWNLOAD -> recordClick(
                    request.profileId(),
                    request.linkLabel() != null ? request.linkLabel() : "vCard",
                    request.visitorId(),
                    type);
        }
    }

    private PublicCardResponse mapToPublicResponse(CardProfile profile) {
        return PublicCardResponse.builder()
                .id(profile.getId())
                .slug(profile.getSlug())
                .templateId(profile.getTemplateId())
                .profileData(profile.getProfileData())
                .aiAvatarUrl(profile.getAiAvatarUrl())
                .address(profile.getAddress())
                .socialLinks(profile.getSocialLinks())
                .removeWatermark(resolveRemoveWatermark(profile.getOwnerEmail()))
                .build();
    }

    /**
     * Resolves the owner's removeWatermark Pro preference from user-service.
     * Fails closed: any lookup error (owner never set, user-service down, bad
     * internal key) keeps the watermark visible rather than hiding it by mistake.
     * The result is cached with the public card (TTL 300s), so a preference
     * change propagates within the cache window.
     */
    private boolean resolveRemoveWatermark(String ownerEmail) {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            return false;
        }
        try {
            UserWatermarkResponse response = userServiceClient.getWatermark(ownerEmail, internalApiKey);
            return response != null && response.removeWatermark();
        } catch (Exception e) {
            log.warn("Could not resolve watermark preference for owner '{}': {}", ownerEmail, e.getMessage());
            return false;
        }
    }

    private CardResponse mapToResponse(CardProfile profile) {
        return CardResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .slug(profile.getSlug())
                .templateId(profile.getTemplateId())
                .profileData(profile.getProfileData())
                .aiAvatarUrl(profile.getAiAvatarUrl())
                .address(profile.getAddress())
                .socialLinks(profile.getSocialLinks())
                .isActive(profile.getIsActive())
                .build();
    }

    private String convertToJsonString(Map<String, Object> profileData) {
        if (profileData == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(profileData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize profile data to JSON string", e);
        }
    }
}