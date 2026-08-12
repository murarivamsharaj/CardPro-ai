package com.cardpro.card.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the JSON contract of the public card DTO: the {@code id} field must
 * be present in the serialized response (as a UUID string), because the
 * public lead form on the frontend depends on it.
 */
class PublicCardResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesIdAsUuidString() throws Exception {
        UUID id = UUID.randomUUID();
        PublicCardResponse response = PublicCardResponse.builder()
                .id(id)
                .slug("muari-card")
                .templateId("basic")
                .profileData("{}")
                .build();

        JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(node.has("id")).isTrue();
        assertThat(node.get("id").asText()).isEqualTo(id.toString());
    }
}
