package com.burgosfacundo.inventory.stock_transfer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockTransferRequest(

        @NotNull(message = "Product id is required")
        @Positive(message = "Product id must be positive")
        Long productId,

        @NotNull(message = "Source warehouse id is required")
        @Positive(message = "Source warehouse id must be positive")
        Long sourceWarehouseId,

        @NotNull(message = "Destination warehouse id is required")
        @Positive(message = "Destination warehouse id must be positive")
        Long destinationWarehouseId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity
) {
}