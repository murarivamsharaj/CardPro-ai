package com.cardpro.card.service;

import com.cardpro.card.client.UserServiceClient;
import com.cardpro.card.dto.response.PublicCardResponse;
import com.cardpro.card.dto.response.UserWatermarkResponse;
import com.cardpro.card.entity.CardProfile;
import com.cardpro.card.exception.CardNotFoundException;
import com.cardpro.card.repository.CardEventRepository;
import com.cardpro.card.repository.CardProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Regression tests for the public card endpoint: the DTO returned by
 * GET /api/v1/cards/{slug} must always carry the card {@code id} so the
 * public "Contact Me" lead form can attribute submissions to the owner.
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardProfileRepository cardProfileRepository;

    @Mock
    private CardEventRepository cardEventRepository;

    @Mock
    private CardCacheService cardCacheService;

    @Mock
    private SlugService slugService;

    @Mock
    private UserServiceClient userServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardService(
                cardProfileRepository, cardEventRepository, cardCacheService, slugService, objectMapper, userServiceClient);
        // Normally @Value-injected by Spring; wired manually for the unit test.
        ReflectionTestUtils.setField(cardService, "internalApiKey", "cardpro-secret-key");
    }

    @Test
    void getPublicCard_populatesIdFromProfileOnCacheMiss() {
        UUID profileId = UUID.randomUUID();
        CardProfile profile = CardProfile.builder()
                .id(profileId)
                .slug("muari-card")
                .templateId("basic")
                .profileData("{\"fullName\":\"Muari\"}")
                .aiAvatarUrl("https://example.com/avatar.png")
                .build();

        when(cardCacheService.getCachedProfile("muari-card")).thenReturn(null);
        when(cardProfileRepository.findBySlug("muari-card")).thenReturn(Optional.of(profile));

        PublicCardResponse response = cardService.getPublicCard("muari-card");

        assertThat(response.getId()).isEqualTo(profileId);
        assertThat(response.getSlug()).isEqualTo("muari-card");
        assertThat(response.getTemplateId()).isEqualTo("basic");
        assertThat(response.getProfileData()).contains("Muari");
        assertThat(response.getAiAvatarUrl()).isEqualTo("https://example.com/avatar.png");

        // The freshly built response is written back to the cache.
        verify(cardCacheService).cacheProfile("muari-card", response);
    }

    @Test
    void findBySlug_returnsActiveCardDetailsIncludingAddressAndSocialLinks() {
        UUID profileId = UUID.randomUUID();
        CardProfile profile = CardProfile.builder()
                .id(profileId)
                .slug("active-card")
                .templateId("basic")
                .profileData("{\"fullName\":\"Jane\"}")
                .address("123 Main Street, Bengaluru")
                .socialLinks(Map.of("linkedin", "https://linkedin.com/in/jane"))
                .isActive(true)
                .build();
        when(cardProfileRepository.findBySlug("active-card")).thenReturn(Optional.of(profile));

        PublicCardResponse response = cardService.findBySlug("active-card");

        assertThat(response.getId()).isEqualTo(profileId);
        assertThat(response.getAddress()).isEqualTo("123 Main Street, Bengaluru");
        assertThat(response.getSocialLinks()).containsEntry("linkedin", "https://linkedin.com/in/jane");
    }

    @Test
    void findBySlug_rejectsDeactivatedCards() {
        CardProfile profile = CardProfile.builder()
                .slug("offline-card")
                .profileData("{}")
                .isActive(false)
                .build();
        when(cardProfileRepository.findBySlug("offline-card")).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> cardService.findBySlug("offline-card"))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void findBySlug_hidesWatermarkWhenOwnerPreferenceIsOn() {
        CardProfile profile = CardProfile.builder()
                .slug("pro-card")
                .profileData("{}")
                .ownerEmail("owner@example.com")
                .isActive(true)
                .build();
        when(cardProfileRepository.findBySlug("pro-card")).thenReturn(Optional.of(profile));
        when(userServiceClient.getWatermark(eq("owner@example.com"), anyString()))
                .thenReturn(new UserWatermarkResponse(true));

        PublicCardResponse response = cardService.findBySlug("pro-card");

        assertThat(response.isRemoveWatermark()).isTrue();
    }

    @Test
    void findBySlug_keepsWatermarkWhenOwnerUnknownOrLookupFails() {
        CardProfile profile = CardProfile.builder()
                .slug("free-card")
                .profileData("{}")
                .isActive(true)
                .build();
        when(cardProfileRepository.findBySlug("free-card")).thenReturn(Optional.of(profile));

        // No owner email → watermark stays visible without calling user-service.
        PublicCardResponse response = cardService.findBySlug("free-card");
        assertThat(response.isRemoveWatermark()).isFalse();
        verifyNoInteractions(userServiceClient);
    }

    @Test
    void getPublicCard_returnsCachedProfileWithIdWithoutTouchingDatabase() {
        PublicCardResponse cached = PublicCardResponse.builder()
                .id(UUID.randomUUID())
                .slug("muari-card")
                .templateId("basic")
                .profileData("{}")
                .build();

        when(cardCacheService.getCachedProfile("muari-card")).thenReturn(cached);

        PublicCardResponse response = cardService.getPublicCard("muari-card");

        assertThat(response).isSameAs(cached);
        assertThat(response.getId()).isNotNull();
        verifyNoInteractions(cardProfileRepository);
    }
}
