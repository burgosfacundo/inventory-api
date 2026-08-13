package com.burgosfacundo.inventory.stock.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class MinimumStockInvalidException
        extends BadRequestException {

    public MinimumStockInvalidException() {
        super(
                "Minimum stock cannot be negative",
                "MINIMUM_STOCK_INVALID"
        );
    }
}