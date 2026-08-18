package com.cardpro.ai.service;

import com.cardpro.ai.dto.request.CardDetailsGenerationRequest;
import com.cardpro.ai.dto.response.CardDetailsGenerationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardDetailsGenerationService {

    public CardDetailsGenerationResponse generateCardDetails(String userId, CardDetailsGenerationRequest request) {
        log.info("Bypassing external API for immediate fallback response");
        return buildSmartFallback(request);
    }

    private CardDetailsGenerationResponse buildSmartFallback(CardDetailsGenerationRequest request) {
        String prompt = (request != null && request.getPrompt() != null) ? request.getPrompt() : "Professional";

        List<String> keywords = Arrays.stream(prompt.split("[,\\n;]"))
                .map(String::trim)
                .filter(k -> !k.isEmpty())
                .limit(5)
                .collect(Collectors.toList());

        String primary = keywords.isEmpty() ? "Professional" : keywords.get(0);
        String primaryTitle = titleCase(primary);

        String tagline;
        if (keywords.size() >= 2) {
            tagline = "Expert in " + primaryTitle + " & " + titleCase(keywords.get(1)) + " — let's connect.";
        } else if (keywords.size() == 1) {
            tagline = "Expert in " + primaryTitle + " — helping you succeed.";
        } else {
            tagline = "Professional & passionate — let's connect.";
        }

        StringBuilder bio = new StringBuilder();
        if (keywords.isEmpty()) {
            bio.append("I deliver quality service and build lasting relationships.");
        } else {
            bio.append("I specialize in ").append(String.join(", ", keywords))
                    .append(" and I'm passionate about delivering quality work.");
        }
        bio.append(" I help clients reach their goals with a fresh, dependable approach.");

        if (request != null && request.getTone() != null && !request.getTone().isBlank()) {
            bio.append(" Everything I do carries a ").append(request.getTone().toLowerCase()).append(" touch.");
        }

        return CardDetailsGenerationResponse.builder()
                .suggestedJobTitle("Expert " + primaryTitle)
                .suggestedTagline(tagline)
                .suggestedBio(bio.toString())
                .model("safe-fallback")
                .fallback(true)
                .build();
    }

    private String titleCase(String value) {
        if (value == null || value.isEmpty()) return "";
        return Arrays.stream(value.trim().split("\\s+"))
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }
}