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

    /**
     * Account email of the card's owner, captured from the JWT at create time.
     * Lets card-service resolve the owner's user-service preferences (e.g.
     * removeWatermark) when rendering the public card — the card's UUID key and
     * user-service's email key have no direct join.
     */
    @Column(name = "owner_email", length = 255)
    private String ownerEmail;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "template_id", nullable = false, length = 50)
    @Builder.Default
    private String templateId = "basic";

    // Replaced the legacy @Type with Hibernate 6's native JSON mapping
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "profile_data", columnDefinition = "jsonb", nullable = false)
    private String profileData;

    @Column(name = "ai_avatar_url", length = 500)
    private String aiAvatarUrl;

    // Physical / office address rendered on the public card. Optional.
    @Column(name = "address", length = 500)
    private String address;

    // Flexible social media: platform key ("linkedin", "github", "twitter",
    // "instagram", "youtube", "website", "whatsapp", ...) -> profile URL.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_links", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> socialLinks = new HashMap<>();

    // Added to fix the incrementViewCount() error in your InternalCardController
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

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