package com.burgosfacundo.inventory.product_supplier.dto;


public record ProductSupplierResponse(Long id, ProductSummaryResponse product, SupplierSummaryResponse supplier) {
}
