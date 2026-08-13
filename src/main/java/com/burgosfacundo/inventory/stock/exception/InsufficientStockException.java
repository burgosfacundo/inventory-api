package com.burgosfacundo.inventory.stock.exception;

import com.burgosfacundo.inventory.common.exception.ConflictException;

public class InsufficientStockException extends ConflictException {

    public InsufficientStockException(
            int available,
            int requested
    ) {
        super(
                "Insufficient stock. Available: "
                        + available
                        + ", requested: "
                        + requested,
                "INSUFFICIENT_STOCK"
        );
    }
}