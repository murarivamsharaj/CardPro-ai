package com.cardpro.productservice.service;

import com.cardpro.productservice.dto.ProductRequest;
import com.cardpro.productservice.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    Page<ProductResponse> getAllProducts(Pageable pageable);

    Page<ProductResponse> searchProducts(String keyword, Pageable pageable);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}