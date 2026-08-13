package com.burgosfacundo.inventory.stock.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class StockQuantityInvalidException
        extends BadRequestException {

    public StockQuantityInvalidException() {
        super(
                "Stock quantity cannot be negative",
                "STOCK_QUANTITY_INVALID"
        );
    }
}