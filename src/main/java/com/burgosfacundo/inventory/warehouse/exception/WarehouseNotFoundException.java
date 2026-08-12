package com.burgosfacundo.inventory.warehouse.exception;

import com.burgosfacundo.inventory.common.exception.ResourceNotFoundException;

public class WarehouseNotFoundException extends ResourceNotFoundException {
    public WarehouseNotFoundException(Long id) {
        super("Warehouse not found with id: " + id,
                "WAREHOUSE_NOT_FOUND");
    }
}
