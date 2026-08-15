package com.cardpro.card.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single analytics event (VIEW or CLICK) recorded against a card profile.
 * This is the raw data backing the dashboard's time-series charts:
 * <ul>
 *   <li>{@code eventAt} + {@code eventType} — daily view/click volume</li>
 *   <li>{@code linkLabel} — per-link click performance (e.g. "LinkedIn")</li>
 *   <li>{@code visitorId} — optional, caller-supplied session id for unique-visitor counts</li>
 * </ul>
 */
@Entity
@Table(name = "card_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 10)
    private CardEventType eventType;

    @Column(name = "visitor_id", length = 64)
    private String visitorId;

    @Column(name = "link_label", length = 100)
    private String linkLabel;

    @Column(name = "event_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime eventAt = LocalDateTime.now();
}
