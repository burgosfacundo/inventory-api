package com.burgosfacundo.inventory.product_supplier.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductSupplierRequest(
                                    @Positive(message = "Product id must be positive")
                                    @NotNull(message = "Product id cannot be null")
                                    Long productId,
                                    @Positive(message = "Supplier id must be positive")
                                    @NotNull(message = "Supplier id cannot be null")
                                    Long supplierId
) {
}
