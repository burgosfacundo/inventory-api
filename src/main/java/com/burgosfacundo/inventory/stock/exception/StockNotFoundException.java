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


    public StockNotFoundException(Long productId, Long warehouseId) {
        super(
                "Stock not found for product id "
                        + productId
                        + " and warehouse id "
                        + warehouseId,
                "STOCK_NOT_FOUND"
        );
    }
}