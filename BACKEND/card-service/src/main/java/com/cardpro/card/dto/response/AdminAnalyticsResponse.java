package com.cardpro.card.dto.response;

/**
 * Platform-wide metrics for the admin analytics view, aggregated across every
 * card in the system (not scoped to one owner).
 *
 * @param totalCards      total number of card profiles
 * @param activeCards     profiles currently marked active
 * @param totalViews      sum of the cumulative view counters across all cards
 * @param viewsLast7Days  VIEW events recorded in the last 7 days
 */
public record AdminAnalyticsResponse(
        long totalCards,
        long activeCards,
        long totalViews,
        long viewsLast7Days
) {
}
