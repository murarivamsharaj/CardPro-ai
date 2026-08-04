package com.cardpro.productservice.repository;

import com.cardpro.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import org.springframework.data.domain.Pageable;

/**
 * Repository interface for Product entity.
 * Provides standard CRUD operations via JpaRepository.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
