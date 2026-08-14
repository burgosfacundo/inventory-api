package com.burgosfacundo.inventory.stock_transfer.exception;

import com.burgosfacundo.inventory.common.exception.ResourceNotFoundException;

public class StockTransferNotFoundException extends ResourceNotFoundException {
    public StockTransferNotFoundException(Long id) {
        super(
                "Stock transfer not found with id: " + id,
                "STOCK_TRANSFER_NOT_FOUND");
    }
}
