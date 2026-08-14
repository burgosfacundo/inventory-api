package com.burgosfacundo.inventory.product_supplier.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductSupplierPriceRequest(
        @NotNull(message = "Purchase price is required")
        @PositiveOrZero(message = "Purchase price cannot be negative")
        BigDecimal purchasePrice
) {
}