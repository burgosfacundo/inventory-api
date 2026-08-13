package com.burgosfacundo.inventory.stock.exception;

import com.burgosfacundo.inventory.common.exception.ResourceNotFoundException;

public class StockNotFoundException
        extends ResourceNotFoundException {

    public StockNotFoundException(Long id) {
        super(
                "Stock not found with id: " + id,
                "STOCK_NOT_FOUND"
        );
    }
}