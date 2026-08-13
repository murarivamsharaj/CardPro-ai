package com.cardpro.card.controller;

import com.cardpro.card.dto.response.AnalyticsResponse;
import com.cardpro.card.dto.response.TotalCardsResponse;
import com.cardpro.card.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsResponse> getUserAnalytics(@RequestHeader("X-User-Id") String userId) {
        AnalyticsResponse response = analyticsService.getAnalyticsForUser(userId);
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
}