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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
        PublicCardResponse cached = cardCacheService.getCachedProfile(slug);
        if (cached != null) {
            return cached;
        }

        PublicCardResponse response = findBySlug(slug);
        cardCacheService.cacheProfile(slug, response);
        return response;
    }

    public PublicCardResponse findBySlug(String slug) {
        CardProfile profile = cardProfileRepository.findBySlug(slug)
                .orElseThrow(CardNotFoundException::new);

        if (Boolean.FALSE.equals(profile.getIsActive())) {
            throw new CardNotFoundException();
        }

        return mapToPublicResponse(profile);
    }

    /**
     * Returns a list of cards belonging to the user using findAllByUserId,
     * preventing the NonUniqueResultException crash when a user owns multiple cards.
     */
    public List<CardResponse> getCardsByUserId(String userId) {
        List<CardProfile> profiles = cardProfileRepository.findAllByUserId(UUID.fromString(userId));
        if (profiles.isEmpty()) {
            throw new CardNotFoundException();
        }
        return profiles.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Maintained for backwards compatibility using Optional findByUserId
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
                .gender(request.getGender())
                .socialLinks(request.getSocialLinks() != null ? request.getSocialLinks() : new HashMap<>())
                .build();

        profile = cardProfileRepository.save(profile);
        return mapToResponse(profile);
    }

    public CardResponse updateCard(String userId, String ownerEmail, UpdateCardRequest request) {
        // Fetch user's cards safely using findAllByUserId
        List<CardProfile> profiles = cardProfileRepository.findAllByUserId(UUID.fromString(userId));
        if (profiles.isEmpty()) {
            throw new CardNotFoundException();
        }
        CardProfile profile = profiles.get(0); // Updates the primary/first card

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
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getSocialLinks() != null) {
            profile.setSocialLinks(request.getSocialLinks());
        }
        if (request.getIsActive() != null) {
            profile.setIsActive(request.getIsActive());
        }
        if (ownerEmail != null && !ownerEmail.isBlank()) {
            profile.setOwnerEmail(ownerEmail);
        }

        profile = cardProfileRepository.save(profile);
        cardCacheService.evictCache(profile.getSlug());
        return mapToResponse(profile);
    }

    public void deleteCard(String userId) {
        List<CardProfile> profiles = cardProfileRepository.findAllByUserId(UUID.fromString(userId));
        if (profiles.isEmpty()) {
            throw new CardNotFoundException();
        }
        CardProfile profile = profiles.get(0);

        cardProfileRepository.delete(profile);
        cardCacheService.evictCache(profile.getSlug());
    }

    @Transactional
    public void incrementViewCount(UUID profileId) {
        incrementViewCount(profileId, null);
    }

    @Transactional
    public void incrementViewCount(UUID profileId, String visitorId) {
        incrementViewCount(profileId, visitorId, CardEventType.VIEW);
    }

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

        cardCacheService.evictCache(profile.getSlug());
    }

    @Transactional
    public void recordClick(UUID profileId, String linkLabel, String visitorId) {
        recordClick(profileId, linkLabel, visitorId, CardEventType.CLICK);
    }

    @Transactional
    public void recordClick(UUID profileId, String linkLabel, String visitorId, CardEventType eventType) {
        cardProfileRepository.findById(profileId)
                .orElseThrow(CardNotFoundException::new);

        cardEventRepository.save(CardEvent.builder()
                .profileId(profileId)
                .eventType(eventType)
                .linkLabel(linkLabel)
                .visitorId(visitorId)
                .build());
    }

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
                .gender(profile.getGender())
                .socialLinks(profile.getSocialLinks())
                .removeWatermark(resolveRemoveWatermark(profile.getOwnerEmail()))
                .premiumTemplatesUnlocked(profile.isPremiumTemplatesUnlocked())
                .build();
    }

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
                .gender(profile.getGender())
                .socialLinks(profile.getSocialLinks())
                .isActive(profile.getIsActive())
                .premiumTemplatesUnlocked(profile.isPremiumTemplatesUnlocked())
                .leadCredits(profile.getLeadCredits())
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