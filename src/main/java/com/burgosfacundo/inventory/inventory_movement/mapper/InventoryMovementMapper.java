package com.burgosfacundo.inventory.inventory_movement.mapper;

import com.burgosfacundo.inventory.inventory_movement.dto.InventoryMovementRequest;
import com.burgosfacundo.inventory.inventory_movement.dto.InventoryMovementResponse;
import com.burgosfacundo.inventory.inventory_movement.model.InventoryMovement;
import com.burgosfacundo.inventory.product.dto.ProductSummaryResponse;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseSummaryResponse;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryMovementMapper {

    public static InventoryMovement toEntity(InventoryMovementRequest request, Product product, Warehouse warehouse) {
        return new InventoryMovement(product, warehouse, request.type(), request.quantity());
    }

    public static InventoryMovementResponse toResponse(InventoryMovement movement) {
        Product product = movement.getProduct();

        Warehouse warehouse = movement.getWarehouse();

        ProductSummaryResponse productResponse = new ProductSummaryResponse(product.getId(), product.getSku(), product.getName());

        WarehouseSummaryResponse warehouseResponse = new WarehouseSummaryResponse(warehouse.getId(), warehouse.getCode(), warehouse.getName());

        return new InventoryMovementResponse(movement.getId(), productResponse, warehouseResponse,
                movement.getType(), movement.getQuantity(), movement.getCreatedAt());
    }
}