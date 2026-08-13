package com.burgosfacundo.inventory.stock.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class StockAdjustmentQuantityInvalidException extends BadRequestException {

    public StockAdjustmentQuantityInvalidException() {
        super(
                "Stock adjustment quantity must be greater than zero",
                "STOCK_ADJUSTMENT_QUANTITY_INVALID"
        );
    }
}