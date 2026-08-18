package com.cardpro.card.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "card_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "owner_email", length = 255)
    private String ownerEmail;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "template_id", nullable = false, length = 50)
    @Builder.Default
    private String templateId = "basic";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "profile_data", columnDefinition = "jsonb", nullable = false)
    private String profileData;

    @Column(name = "ai_avatar_url", length = 500)
    private String aiAvatarUrl;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "gender", length = 50)
    private String gender;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_links", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> socialLinks = new HashMap<>();

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "premium_templates_unlocked", nullable = false)
    @Builder.Default
    private boolean premiumTemplatesUnlocked = false;

    @Column(name = "lead_credits", nullable = false)
    @Builder.Default
    private int leadCredits = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}