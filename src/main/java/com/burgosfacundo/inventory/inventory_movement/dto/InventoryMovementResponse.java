package com.burgosfacundo.inventory.inventory_movement.dto;

import com.burgosfacundo.inventory.inventory_movement.model.MovementType;
import com.burgosfacundo.inventory.product.dto.ProductSummaryResponse;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseSummaryResponse;

import java.time.LocalDateTime;

public record InventoryMovementResponse(
        Long id,
        ProductSummaryResponse product,
        WarehouseSummaryResponse warehouse,
        MovementType type,
        int quantity,
        LocalDateTime createdAt
) {
}