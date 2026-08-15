package com.cardpro.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    /** Display name shown on the user's profile/settings (nullable). */
    private String displayName;

    /** Job title shown on the user's profile/settings (nullable). */
    private String jobTitle;

    private String phoneNumber;

    private String profileImage;

    @Column(nullable = false)
    private String role; // e.g., "ROLE_USER" or "ROLE_ADMIN"

    @Builder.Default
    private boolean active = true;

    /** Whether the user has paid for the CardPro Pro subscription. */
    @Builder.Default
    private boolean isPro = false;

    /**
     * Pro perk: when true, the "Powered by CardPro" watermark footer is hidden
     * on this user's public cards (consumed by card-service at card render).
     */
    @Builder.Default
    private boolean removeWatermark = false;

    /** Master preference consumed by the lead-notification pipeline. */
    @Builder.Default
    private Boolean emailNotificationsEnabled = true;

    /**
     * Developer integration: secret API key used to authenticate external
     * integrations (e.g. a future CRM lead-forwarding webhook). Auto-generated
     * as a UUID the first time the profile is read/created; can be regenerated
     * by the owner from Settings.
     */
    private String apiKey;

    /**
     * Developer integration: webhook URL that future CRM lead-forwarding
     * integrations can POST new leads to. Null until the owner saves one.
     */
    private String webhookUrl;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}