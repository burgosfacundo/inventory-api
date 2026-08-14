package com.burgosfacundo.inventory.product_supplier.dto;


import com.burgosfacundo.inventory.product.dto.ProductSummaryResponse;

import java.math.BigDecimal;

public record ProductSupplierResponse(
        Long id,
        ProductSummaryResponse product,
        SupplierSummaryResponse supplier,
        BigDecimal purchasePrice) {
}
