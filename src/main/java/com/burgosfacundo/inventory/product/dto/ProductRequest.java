package com.burgosfacundo.inventory.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU must be at most 50 characters long")
        String sku,
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters long")
        String name,
        String description,
        @NotNull(message = "Sale price is required")
        @PositiveOrZero(message = "Sale price cannot be negative")
        BigDecimal salePrice,
        @NotNull(message = "Category ID is required")
        @Positive(message = "Category ID must be a positive number")
        Long idCategory
) {
}
