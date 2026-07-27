package com.cardpro.lead.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(name = "visitor_name", nullable = false, length = 150)
    private String visitorName;

    @Column(name = "visitor_phone", nullable = false, length = 20)
    private String visitorPhone;

    @Column(name = "ai_followup", columnDefinition = "TEXT")
    private String aiFollowup;

    @Column(name = "captured_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime capturedAt = LocalDateTime.now();
}
