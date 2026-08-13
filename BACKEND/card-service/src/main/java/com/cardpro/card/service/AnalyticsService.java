package com.cardpro.card.service;

import com.cardpro.card.dto.response.AnalyticsResponse;
import com.cardpro.card.repository.CardProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CardProfileRepository cardProfileRepository;

    /**
     * Absolute total count of every digital card in the system (admin metric).
     */
    public long getTotalCardCount() {
        return cardProfileRepository.count();
    }

    public AnalyticsResponse getAnalyticsForUser(String userId) {
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