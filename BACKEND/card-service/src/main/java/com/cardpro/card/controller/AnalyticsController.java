package com.cardpro.card.controller;

import com.cardpro.card.dto.response.AnalyticsResponse;
import com.cardpro.card.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsResponse> getUserAnalytics(@RequestHeader("X-User-Id") Long userId) {
        AnalyticsResponse response = analyticsService.getAnalyticsForUser(userId);
        return ResponseEntity.ok(response);
    }
}