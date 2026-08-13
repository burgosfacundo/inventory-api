package com.burgosfacundo.inventory.product.dto;

import com.burgosfacundo.inventory.category.dto.CategorySummaryResponse;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal salePrice,
        Boolean active,
        CategorySummaryResponse category
) {
}
