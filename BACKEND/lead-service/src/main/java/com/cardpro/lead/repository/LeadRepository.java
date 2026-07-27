package com.cardpro.lead.repository;

import com.cardpro.lead.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Page<Lead> findByProfileId(UUID profileId, Pageable pageable);

    long countByProfileId(UUID profileId);
}
