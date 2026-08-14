package com.burgosfacundo.inventory.product_supplier.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductSupplierRequest(
                                    @Positive(message = "Product id must be positive")
                                    @NotNull(message = "Product id cannot be null")
                                    Long productId,
                                    @Positive(message = "Supplier id must be positive")
                                    @NotNull(message = "Supplier id cannot be null")
                                    Long supplierId,
                                    @NotNull(message = "Purchase price is required")
                                    @PositiveOrZero(message = "Purchase price cannot be negative")
                                    BigDecimal purchasePrice
) {
}
