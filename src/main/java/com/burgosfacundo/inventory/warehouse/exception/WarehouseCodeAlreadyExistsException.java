package com.burgosfacundo.inventory.warehouse.exception;

import com.burgosfacundo.inventory.common.exception.ConflictException;

public class WarehouseCodeAlreadyExistsException extends ConflictException {
    public WarehouseCodeAlreadyExistsException(String code) {
        super("Warehouse with code: " + code + " already exists.",
                "WAREHOUSE_CODE_ALREADY_EXISTS");
    }
}
