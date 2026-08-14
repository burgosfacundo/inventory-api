package com.burgosfacundo.inventory.stock_transfer.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class SameWarehouseTransferException
        extends BadRequestException {

    public SameWarehouseTransferException() {
        super(
                "Source and destination warehouses must be different",
                "SAME_WAREHOUSE_TRANSFER"
        );
    }
}