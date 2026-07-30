package com.cardpro.productservice.exception;

/**
 * Exception thrown when a requested product is not found in the database.
 */
public class ProductNotFoundException extends RuntimeException {

    private final Long productId;

    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
