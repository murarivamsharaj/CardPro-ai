package com.cardpro.card.repository;

import com.cardpro.card.entity.CardProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardProfileRepository extends JpaRepository<CardProfile, UUID> {

    Optional<CardProfile> findBySlug(String slug);

    Optional<CardProfile> findByUserId(UUID userId);

    boolean existsBySlug(String slug);
}
