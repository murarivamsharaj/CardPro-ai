package com.cardpro.ai.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GeminiClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final int timeoutSeconds;

    public GeminiClient(
            WebClient geminiWebClient,
            @Value("${app.ai.gemini.api-key}") String apiKey,
            @Value("${app.ai.gemini.model:gemini-3.6-flash}") String model,
            @Value("${app.ai.gemini.timeout-seconds:30}") int timeoutSeconds) {
        this.webClient = geminiWebClient;
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;
    }

    public String generateCardDetails(String prompt, String tone) {
        String keywords = tone != null && !tone.isBlank() ? prompt + " (tone: " + tone + ")" : prompt;

        String instruction = "You are a professional copywriter. Create custom digital business card details "
                + "for the keywords: '"
                + keywords
                + "'. Return strictly a valid JSON object with keys: 'suggestedJobTitle', "
                + "'suggestedTagline', 'suggestedBio'. Do not wrap in markdown code fences.";

        GenerateContentRequest request = new GenerateContentRequest(
                List.of(new Content(List.of(new ContentPart(instruction)))),
                new GenerationConfig("application/json", 0.7)
        );

        GenerateContentResponse response;
        try {
            response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/" + model + ":generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GenerateContentResponse.class)
                    .block(Duration.ofSeconds(timeoutSeconds));
        } catch (WebClientResponseException ex) {
            log.error("Gemini API Error: Status={}, Body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        }

        return extractText(response);
    }

    private String extractText(GenerateContentResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            return null;
        }
        Candidate candidate = response.candidates().get(0);
        if (candidate == null || candidate.content() == null || candidate.content().parts() == null) {
            return null;
        }
        String text = candidate.content().parts().stream()
                .map(ContentPart::text)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
        return stripMarkdownFences(text);
    }

    private String stripMarkdownFences(String text) {
        if (text == null) return null;
        String cleaned = text.trim();
        cleaned = cleaned.replaceAll("(?s)^```[a-zA-Z0-9]*\\s*", "").replaceAll("(?s)\\s*```$", "");
        return cleaned.trim();
    }

    private record GenerateContentRequest(List<Content> contents, GenerationConfig generationConfig) {}
    private record GenerationConfig(String responseMimeType, double temperature) {}
    private record Content(List<ContentPart> parts) {}
    private record ContentPart(String text) {}
    private record GenerateContentResponse(List<Candidate> candidates) {}
    private record Candidate(Content content) {}
}