package com.burgosfacundo.inventory.product.service;

import com.burgosfacundo.inventory.product.dto.ProductRequest;
import com.burgosfacundo.inventory.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductService {
    ProductResponse save(ProductRequest request);
    ProductResponse findById(Long id);
    Page<ProductResponse> findAll(Long categoryId, Boolean active, Pageable pageable);
    ProductResponse update(Long id, ProductRequest request);
    ProductResponse updateStatus(Long id, boolean active);
    void delete(Long id);
}
