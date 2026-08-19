package com.cardpro.card.repository;

import com.cardpro.card.entity.CardProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardProfileRepository extends JpaRepository<CardProfile, UUID> {

    // Safe single card lookup: uses 'findFirst' to prevent NonUniqueResultException if a user has multiple cards
    Optional<CardProfile> findFirstByUserId(UUID userId);

    // Multiple cards by user ID (used by AnalyticsService)
    List<CardProfile> findAllByUserId(UUID userId);

    // Lookup by public slug
    Optional<CardProfile> findBySlug(String slug);

    // Slug existence check
    boolean existsBySlug(String slug);

    // Search cards with pagination (used by CardService admin/search endpoints)
    Page<CardProfile> findBySlugContainingIgnoreCase(String slug, Pageable pageable);

    // Plain search list
    List<CardProfile> findBySlugContainingIgnoreCase(String slug);

    // Admin count active cards
    long countByIsActiveTrue();

    // Admin total views calculation
    @Query("SELECT COALESCE(SUM(c.viewCount), 0) FROM CardProfile c")
    long sumViewCount();
}