package com.burgosfacundo.inventory.inventory_movement.dto;

import com.burgosfacundo.inventory.inventory_movement.model.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventoryMovementRequest(

        @NotNull(message = "Product id is required")
        @Positive(message = "Product id must be positive")
        Long productId,

        @NotNull(message = "Warehouse id is required")
        @Positive(message = "Warehouse id must be positive")
        Long warehouseId,

        @NotNull(message = "Movement type is required")
        MovementType type,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity
) {
}