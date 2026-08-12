package com.cardpro.card.service;

import com.cardpro.card.dto.request.CreateCardRequest;
import com.cardpro.card.dto.request.UpdateCardRequest;
import com.cardpro.card.dto.response.CardResponse;
import com.cardpro.card.dto.response.PublicCardResponse;
import com.cardpro.card.exception.CardNotFoundException;
import com.cardpro.card.entity.CardProfile;
import com.cardpro.card.repository.CardProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardProfileRepository cardProfileRepository;
    private final CardCacheService cardCacheService;
    private final SlugService slugService;
    private final ObjectMapper objectMapper;

    public PublicCardResponse getPublicCard(String slug) {
        // Try cache first
        PublicCardResponse cached = cardCacheService.getCachedProfile(slug);
        if (cached != null) {
            return cached;
        }

        CardProfile profile = cardProfileRepository.findBySlug(slug)
                .orElseThrow(CardNotFoundException::new);

        PublicCardResponse response = PublicCardResponse.builder()
                .id(profile.getId())
                .slug(profile.getSlug())
                .templateId(profile.getTemplateId())
                .profileData(profile.getProfileData())
                .aiAvatarUrl(profile.getAiAvatarUrl())
                .build();

        cardCacheService.cacheProfile(slug, response);
        return response;
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

    public CardResponse createCard(String userId, CreateCardRequest request) {
        slugService.validateSlug(request.getSlug());

        CardProfile profile = CardProfile.builder()
                .userId(UUID.fromString(userId))
                .slug(request.getSlug())
                .templateId(request.getTemplateId() != null ? request.getTemplateId() : "basic")
                .profileData(convertToJsonString(request.getProfileData()))
                .build();

        profile = cardProfileRepository.save(profile);
        return mapToResponse(profile);
    }

    public CardResponse updateCard(String userId, UpdateCardRequest request) {
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
        if (request.getIsActive() != null) {
            profile.setIsActive(request.getIsActive());
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

    public void incrementViewCount(UUID profileId) {
        CardProfile profile = cardProfileRepository.findById(profileId)
                .orElseThrow(CardNotFoundException::new);

        // Assuming your CardProfile entity now has a viewCount field
        profile.setViewCount(profile.getViewCount() + 1);
        cardProfileRepository.save(profile);

        // Evict the cache so the updated view count shows on the next request
        cardCacheService.evictCache(profile.getSlug());
    }

    private CardResponse mapToResponse(CardProfile profile) {
        return CardResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .slug(profile.getSlug())
                .templateId(profile.getTemplateId())
                .profileData(profile.getProfileData())
                .aiAvatarUrl(profile.getAiAvatarUrl())
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