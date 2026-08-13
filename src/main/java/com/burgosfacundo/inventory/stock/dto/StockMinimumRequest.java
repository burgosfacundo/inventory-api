package com.burgosfacundo.inventory.stock.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockMinimumRequest(

        @NotNull(message = "Minimum stock is required")
        @PositiveOrZero(
                message = "Minimum stock cannot be negative"
        )
        Integer minimumStock
) {
}