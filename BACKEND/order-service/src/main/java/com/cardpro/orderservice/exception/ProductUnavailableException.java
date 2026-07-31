package com.cardpro.orderservice.exception;

/**
 * Exception thrown when the product-service call fails or returns no product
 * while trying to create an order. Indicates the referenced product is
 * unavailable or the inter-service call could not be completed.
 */
public class ProductUnavailableException extends RuntimeException {

    private final Long productId;

    public ProductUnavailableException(Long productId) {
        super("Product is unavailable with id: " + productId);
        this.productId = productId;
    }

    public ProductUnavailableException(Long productId, String message) {
        super(message);
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
