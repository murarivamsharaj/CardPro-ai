package com.cardpro.productservice.service;

import com.cardpro.productservice.dto.ProductRequest;
import com.cardpro.productservice.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}
