package com.cardpro.card.service;

import com.cardpro.card.dto.response.AnalyticsResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AnalyticsService {

    public AnalyticsResponse getAnalyticsForUser(Long userId) {
        // TODO: Replace with actual database queries and repository calls later
        return AnalyticsResponse.builder()
                .totalViews(1250)
                .uniqueVisitors(980)
                .totalLeads(45)
                .clickThroughRate(12.5)
                .viewsByDate(new HashMap<>() {{
                    put("2026-08-01", 120L);
                    put("2026-08-02", 150L);
                    put("2026-08-03", 200L);
                }})
                .clicksByLink(new HashMap<>() {{
                    put("LinkedIn", 85L);
                    put("GitHub", 110L);
                }})
                .build();
    }
}