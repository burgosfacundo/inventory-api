package com.burgosfacundo.inventory.product_supplier.service;

import com.burgosfacundo.inventory.product_supplier.dto.ProductSupplierPriceRequest;
import com.burgosfacundo.inventory.product_supplier.dto.ProductSupplierResponse;
import com.burgosfacundo.inventory.product_supplier.dto.ProductSupplierRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSupplierService {
    ProductSupplierResponse save(ProductSupplierRequest request);
    ProductSupplierResponse findById(Long id);
    Page<ProductSupplierResponse> findAll(Long productId, Long supplierId, Pageable pageable);
    ProductSupplierResponse updatePurchasePrice(Long id, ProductSupplierPriceRequest request);
    void delete(Long id);
}
