package com.burgosfacundo.inventory.stock_transfer.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class TransferQuantityInvalidException
        extends BadRequestException {

    public TransferQuantityInvalidException() {
        super(
                "Transfer quantity must be greater than zero",
                "INVALID_TRANSFER_QUANTITY"
        );
    }
}