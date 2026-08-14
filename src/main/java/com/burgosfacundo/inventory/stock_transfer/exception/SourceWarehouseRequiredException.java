package com.burgosfacundo.inventory.stock_transfer.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class SourceWarehouseRequiredException
        extends BadRequestException {

    public SourceWarehouseRequiredException() {
        super(
                "Source warehouse is required",
                "SOURCE_WAREHOUSE_REQUIRED"
        );
    }
}