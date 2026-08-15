package com.cardpro.card.service;

import com.cardpro.card.client.LeadServiceClient;
import com.cardpro.card.dto.response.AdminAnalyticsResponse;
import com.cardpro.card.dto.response.AnalyticsResponse;
import com.cardpro.card.entity.CardEventType;
import com.cardpro.card.entity.CardProfile;
import com.cardpro.card.repository.CardEventRepository;
import com.cardpro.card.repository.CardProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregates the analytics dashboard's metrics directly from the database:
 *
 * <ul>
 *   <li><b>Total views</b> — sum of the cumulative {@code view_count} counters</li>
 *   <li><b>Views by date</b> — VIEW events bucketed per calendar day (zero-filled
 *       so time-series charts render continuous lines)</li>
 *   <li><b>Clicks by link</b> — CLICK events grouped by link label</li>
 *   <li><b>Unique visitors</b> — distinct {@code visitorId}s among VIEW events</li>
 *   <li><b>Leads / conversion</b> — real lead counts fetched from lead-service
 *       over Feign (leads live in lead-service's database, not here)</li>
 * </ul>
 *
 * The user-scoped methods never throw when the caller has no card yet — they
 * return a zeroed response instead, so the dashboard renders empty states
 * rather than 404s.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CardProfileRepository cardProfileRepository;
    private final CardEventRepository cardEventRepository;
    private final LeadServiceClient leadServiceClient;

    /** Event types counted as "views" by the analytics aggregation. */
    private static final Set<CardEventType> VIEW_TYPES = Set.of(CardEventType.VIEW, CardEventType.PAGE_VIEW);

    /** Event types counted as "clicks" (drives the click-through rate). */
    private static final Set<CardEventType> CLICK_TYPES = Set.of(
            CardEventType.CLICK,
            CardEventType.SOCIAL_CLICK,
            CardEventType.BUTTON_CLICK,
            CardEventType.VCF_DOWNLOAD
    );

    @Value("${app.internal.api-key}")
    private String internalApiKey;

    /**
     * Absolute total count of every digital card in the system (admin metric).
     */
    public long getTotalCardCount() {
        return cardProfileRepository.count();
    }

    /**
     * Platform-wide card metrics for the admin analytics view.
     */
    public AdminAnalyticsResponse getAdminOverview() {
        long totalCards = cardProfileRepository.count();
        long activeCards = cardProfileRepository.countByIsActiveTrue();
        long totalViews = cardProfileRepository.sumViewCount();
        long viewsLast7Days = cardEventRepository.countEventsSince(
                VIEW_TYPES, LocalDate.now().minusDays(7).atStartOfDay());
        return new AdminAnalyticsResponse(totalCards, activeCards, totalViews, viewsLast7Days);
    }

    /**
     * Engagement metrics for a single user, aggregated across all of their
     * cards. {@code days} controls the time-series window of {@code viewsByDate}
     * (bounded to 1..365 by the controller).
     */
    public AnalyticsResponse getAnalyticsForUser(String userId, int days) {
        List<CardProfile> cards = cardProfileRepository.findAllByUserId(UUID.fromString(userId));
        List<UUID> profileIds = cards.stream().map(CardProfile::getId).toList();

        if (profileIds.isEmpty()) {
            return AnalyticsResponse.builder()
                    .totalViews(0)
                    .uniqueVisitors(0)
                    .totalLeads(0)
                    .clickThroughRate(0)
                    .viewsByDate(zeroFilledDays(days))
                    .clicksByLink(Map.of())
                    .build();
        }

        // Cumulative impressions (the counter predates the event log, so it is
        // the authoritative "total views" number)
        long totalViews = cards.stream()
                .mapToLong(card -> card.getViewCount() == null ? 0L : card.getViewCount())
                .sum();

        // Daily views from the event log, zero-filled for continuous charting
        LocalDateTime since = LocalDate.now().minusDays(days - 1L).atStartOfDay();
        Map<String, Long> viewsByDate = zeroFilledDays(days);
        for (Object[] row : cardEventRepository.countViewsByDay(profileIds, since)) {
            viewsByDate.put((String) row[0], ((Number) row[1]).longValue());
        }

        // Per-link clicks from the event log (every click-family event type)
        Map<String, Long> clicksByLink = new LinkedHashMap<>();
        long totalClicks = 0;
        for (Object[] row : cardEventRepository.countClicksByLink(profileIds, CLICK_TYPES)) {
            long clicks = ((Number) row[1]).longValue();
            clicksByLink.put((String) row[0], clicks);
            totalClicks += clicks;
        }

        long uniqueVisitors = cardEventRepository.countDistinctVisitors(profileIds, VIEW_TYPES);

        // Leads are owned by lead-service; a failure there must degrade the
        // dashboard gracefully instead of 500ing the whole analytics page.
        long totalLeads = countLeads(profileIds);

        double clickThroughRate = totalViews > 0 ? (totalClicks * 100.0) / totalViews : 0.0;

        return AnalyticsResponse.builder()
                .totalViews(totalViews)
                .uniqueVisitors(uniqueVisitors)
                .totalLeads(totalLeads)
                .clickThroughRate(clickThroughRate)
                .viewsByDate(viewsByDate)
                .clicksByLink(clicksByLink)
                .build();
    }

    private long countLeads(List<UUID> profileIds) {
        long total = 0;
        for (UUID profileId : profileIds) {
            try {
                total += leadServiceClient.countLeads(profileId, internalApiKey);
            } catch (Exception e) {
                log.warn("Analytics: could not fetch lead count from lead-service for profile {}: {}",
                        profileId, e.getMessage());
            }
        }
        return total;
    }

    /**
     * A contiguous {@code [today - (days - 1), today]} window of ISO date keys,
     * all starting at zero. The event query then overwrites the days that
     * actually have traffic.
     */
    private Map<String, Long> zeroFilledDays(int days) {
        Map<String, Long> map = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            map.put(today.minusDays(i).toString(), 0L);
        }
        return map;
    }
}
