package com.burgosfacundo.inventory.stock_transfer.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class DestinationWarehouseRequiredException
        extends BadRequestException {

    public DestinationWarehouseRequiredException() {
        super(
                "Destination warehouse is required",
                "DESTINATION_WAREHOUSE_REQUIRED"
        );
    }
}