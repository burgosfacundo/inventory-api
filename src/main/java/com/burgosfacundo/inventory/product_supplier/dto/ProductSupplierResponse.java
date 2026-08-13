package com.burgosfacundo.inventory.product_supplier.dto;


import com.burgosfacundo.inventory.product.dto.ProductSummaryResponse;

public record ProductSupplierResponse(Long id, ProductSummaryResponse product, SupplierSummaryResponse supplier) {
}
