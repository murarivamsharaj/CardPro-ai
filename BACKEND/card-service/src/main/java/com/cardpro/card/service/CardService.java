package com.cardpro.card.service;

import com.cardpro.card.dto.request.CreateCardRequest;
import com.cardpro.card.dto.request.UpdateCardRequest;
import com.cardpro.card.dto.response.CardResponse;
import com.cardpro.card.dto.response.PublicCardResponse;
import com.cardpro.card.entity.CardProfile;
import com.cardpro.card.repository.CardProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardProfileRepository cardProfileRepository;
    private final CardCacheService cardCacheService;
    private final SlugService slugService;

    public PublicCardResponse getPublicCard(String slug) {
        // Try cache first
        PublicCardResponse cached = cardCacheService.getCachedProfile(slug);
        if (cached != null) {
            return cached;
        }

        CardProfile profile = cardProfileRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Card profile not found"));

        PublicCardResponse response = PublicCardResponse.builder()
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
            .orElseThrow(() -> new RuntimeException("Card profile not found"));
        return mapToResponse(profile);
    }

    public CardResponse getCardById(UUID profileId) {
        CardProfile profile = cardProfileRepository.findById(profileId)
            .orElseThrow(() -> new RuntimeException("Card profile not found"));
        return mapToResponse(profile);
    }

    public CardResponse createCard(String userId, CreateCardRequest request) {
        slugService.validateSlug(request.getSlug());

        CardProfile profile = CardProfile.builder()
            .userId(UUID.fromString(userId))
            .slug(request.getSlug())
            .templateId(request.getTemplateId() != null ? request.getTemplateId() : "basic")
            .profileData(request.getProfileData())
            .build();

        profile = cardProfileRepository.save(profile);
        return mapToResponse(profile);
    }

    public CardResponse updateCard(String userId, UpdateCardRequest request) {
        CardProfile profile = cardProfileRepository.findByUserId(UUID.fromString(userId))
            .orElseThrow(() -> new RuntimeException("Card profile not found"));

        if (request.getSlug() != null) {
            slugService.validateSlug(request.getSlug());
            profile.setSlug(request.getSlug());
        }
        if (request.getTemplateId() != null) profile.setTemplateId(request.getTemplateId());
        if (request.getProfileData() != null) profile.setProfileData(request.getProfileData());
        if (request.getIsActive() != null) profile.setIsActive(request.getIsActive());

        profile = cardProfileRepository.save(profile);
        cardCacheService.evictCache(profile.getSlug());
        return mapToResponse(profile);
    }

    public voi
