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

    Optional<CardProfile> findBySlug(String slug);

    Optional<CardProfile> findByUserId(UUID userId);

    /**
     * Every card owned by a user. The platform currently models one card per
     * user, but analytics is written against a list so it keeps working (and
     * sums correctly) if multi-card accounts are ever enabled.
     */
    List<CardProfile> findAllByUserId(UUID userId);

    boolean existsBySlug(String slug);

    Page<CardProfile> findBySlugContainingIgnoreCase(String keyword, Pageable pageable);

    /** Admin metric: how many cards are currently active. */
    long countByIsActiveTrue();

    /** Admin metric: sum of the cumulative view counters across all cards. */
    @Query("SELECT COALESCE(SUM(c.viewCount), 0) FROM CardProfile c")
    long sumViewCount();
}