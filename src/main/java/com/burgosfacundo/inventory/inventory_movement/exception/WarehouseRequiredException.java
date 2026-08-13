package com.burgosfacundo.inventory.inventory_movement.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class WarehouseRequiredException
        extends BadRequestException {

    public WarehouseRequiredException() {
        super(
                "Warehouse is required",
                "WAREHOUSE_REQUIRED"
        );
    }
}