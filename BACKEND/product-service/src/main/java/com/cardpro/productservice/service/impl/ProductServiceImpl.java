package com.cardpro.productservice.service.impl;

import com.cardpro.productservice.dto.ProductRequest;
import com.cardpro.productservice.dto.ProductResponse;
import com.cardpro.productservice.entity.Product;
import com.cardpro.productservice.exception.ProductNotFoundException;
import com.cardpro.productservice.repository.ProductRepository;
import com.cardpro.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the ProductService interface.
 * Handles the business logic for product CRUD operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.debug("Creating product with name: {}", request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        product = productRepository.save(product);
        log.info("Product created successfully with id: {}", product.getId());

        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.debug("Fetching product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.debug("Fetching all products");

        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.debug("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        product = productRepository.save(product);
        log.info("Product updated successfully with id: {}", product.getId());

        return mapToResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.debug("Deleting product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.delete(product);
        log.info("Product deleted successfully with id: {}", id);
    }

    /**
     * Maps Product entity to ProductResponse DTO.
     *
     * @param product the entity to map
     * @return the response DTO
     */
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
