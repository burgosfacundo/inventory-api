package com.burgosfacundo.inventory.warehouse.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class WarehouseCodeRequiredException extends BadRequestException {
    public WarehouseCodeRequiredException() {
        super("Warehouse code is required",
                "WAREHOUSE_CODE_REQUIRED");

    }
}
