package com.cardpro.card.controller;

import com.cardpro.card.dto.request.AnalyticsEventRequest;
import com.cardpro.card.dto.response.AdminAnalyticsResponse;
import com.cardpro.card.dto.response.AnalyticsResponse;
import com.cardpro.card.dto.response.TotalCardsResponse;
import com.cardpro.card.service.AnalyticsService;
import com.cardpro.card.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CardService cardService;

    /**
     * Public analytics event ingestion, fired by the card viewer without any
     * authentication. Accepts PAGE_VIEW, SOCIAL_CLICK, BUTTON_CLICK and
     * VCF_DOWNLOAD events attributed to a card via its profile id.
     */
    @PostMapping("/events")
    public ResponseEntity<Void> trackEvent(@Valid @RequestBody AnalyticsEventRequest request) {
        cardService.trackEvent(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Same engagement payload as {@code /summary} under the alias the SRS calls
     * "overview"; {@code days} (7 / 30 / 90) bounds the views-by-date window.
     */
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsResponse> getOverview(
            Principal principal,
            @RequestParam(defaultValue = "30") int days) {
        int boundedDays = Math.min(Math.max(days, 1), 365);
        return ResponseEntity.ok(analyticsService.getAnalyticsForUser(principal.getName(), boundedDays));
    }

    /**
     * Engagement metrics for the authenticated caller, scoped to their own
     * cards. The user id comes from the JWT (via {@link Principal}) rather
     * than a request header, so the identity can't be spoofed on direct calls.
     * {@code days} bounds the time-series window (clamped to 1..365).
     */
    @GetMapping("/summary")
    public ResponseEntity<AnalyticsResponse> getUserAnalytics(
            Principal principal,
            @RequestParam(defaultValue = "30") int days) {
        int boundedDays = Math.min(Math.max(days, 1), 365);
        AnalyticsResponse response = analyticsService.getAnalyticsForUser(principal.getName(), boundedDays);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin metrics: absolute total number of digital cards in the system.
     * Protected by method security (the JWT role claim must include ROLE_ADMIN).
     */
    @GetMapping("/admin/total-cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TotalCardsResponse> getTotalCardCount() {
        return ResponseEntity.ok(new TotalCardsResponse(analyticsService.getTotalCardCount()));
    }

    /**
     * Admin metrics: platform-wide card health (totals, active cards, views).
     * ROLE_ADMIN only.
     */
    @GetMapping("/admin/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAnalyticsResponse> getAdminOverview() {
        return ResponseEntity.ok(analyticsService.getAdminOverview());
    }
}
