package com.cardpro.ai.service;

import com.cardpro.ai.client.GeminiClient;
import com.cardpro.ai.dto.request.CardDetailsGenerationRequest;
import com.cardpro.ai.dto.response.CardDetailsGenerationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Powers the "Magic Autofill" feature in the card editor: given a handful of
 * keywords or a rough summary, it asks Google Gemini to suggest a professional
 * bio, a short tagline, and a job title the user can accept or edit.
 *
 * The model is told to return a raw JSON object; the response is parsed
 * defensively (markdown code fences stripped first). If the API call or the
 * payload shape fails for any reason, the error is logged with its exact body
 * and keyword-aware suggestions are derived from the user's own input so the
 * feature degrades gracefully instead of returning a 500 to the UI.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CardDetailsGenerationService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.gemini.model}")
    private String modelName;

    public CardDetailsGenerationResponse generateCardDetails(String userId, CardDetailsGenerationRequest request) {
        try {
            String raw = geminiClient.generateCardDetails(request.getPrompt(), request.getTone());
            return parseResponse(raw);
        } catch (WebClientResponseException e) {
            // Surface the raw Gemini error body so schema/header problems are
            // visible in the terminal, then fall back to keyword-aware
            // suggestions so Magic Autofill never 500s on the user.
            log.error("Gemini API Error: {}", e.getResponseBodyAsString());
            return buildSmartFallback(request);
        } catch (Exception e) {
            log.error("Gemini API Error: {}", e.getMessage(), e);
            return buildSmartFallback(request);
        }
    }

    /**
     * Parses the model's raw output into the three suggested fields. Strips
     * markdown code fences if present and throws when any required key is
     * missing or unparseable so the caller can fall back gracefully.
     */
    private CardDetailsGenerationResponse parseResponse(String raw) throws Exception {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Gemini returned no content");
        }

        String cleaned = stripMarkdownFences(raw);
        JsonNode node = objectMapper.readTree(cleaned);

        String jobTitle = node.path("suggestedJobTitle").asText(null);
        String tagline = node.path("suggestedTagline").asText(null);
        String bio = node.path("suggestedBio").asText(null);

        if (isBlank(jobTitle) || isBlank(tagline) || isBlank(bio)) {
            throw new IllegalArgumentException("Gemini response missing required fields");
        }

        return CardDetailsGenerationResponse.builder()
                .suggestedJobTitle(jobTitle.trim())
                .suggestedTagline(tagline.trim())
                .suggestedBio(bio.trim())
                .model(modelName)
                .fallback(false)
                .build();
    }

    /**
     * Keyword-aware smart fallback: when Gemini is unreachable or returns
     * garbage, tailor the suggestions directly from the user's own keywords so
     * the copy stays personalized and the UI never sees a 500.
     */
    private CardDetailsGenerationResponse buildSmartFallback(CardDetailsGenerationRequest request) {
        List<String> keywords = Arrays.stream(request.getPrompt().split("[,\\n;]"))
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
        if (request.getTone() != null && !request.getTone().isBlank()) {
            bio.append(" Everything I do carries a ").append(request.getTone().toLowerCase()).append(" touch.");
        }

        return CardDetailsGenerationResponse.builder()
                .suggestedJobTitle("Professional " + primaryTitle)
                .suggestedTagline(tagline)
                .suggestedBio(bio.toString())
                .model("fallback")
                .fallback(true)
                .build();
    }

    /** Capitalizes the first letter of each whitespace-separated word. */
    private String titleCase(String value) {
        return Arrays.stream(value.trim().split("\\s+"))
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }

    private String stripMarkdownFences(String text) {
        String cleaned = text.trim();
        // Remove a leading ```json / ``` fence and the trailing ``` fence.
        cleaned = cleaned.replaceAll("(?s)^```[a-zA-Z0-9]*\\s*", "").replaceAll("(?s)\\s*```$", "");
        return cleaned.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
