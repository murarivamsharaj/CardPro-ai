package com.cardpro.card.service;

import com.cardpro.card.dto.response.PublicCardResponse;
import com.cardpro.card.entity.CardProfile;
import com.cardpro.card.repository.CardProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    private CardCacheService cardCacheService;

    @Mock
    private SlugService slugService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardService(cardProfileRepository, cardCacheService, slugService, objectMapper);
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
