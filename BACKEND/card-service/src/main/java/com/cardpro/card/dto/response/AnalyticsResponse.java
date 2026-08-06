package com.cardpro.card.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsResponse {
    private long totalViews;
    private long uniqueVisitors;
    private long totalLeads;
    private double clickThroughRate;
    private Map<String, Long> viewsByDate; // For time-series chart
    private Map<String, Long> clicksByLink; // For link performance
}