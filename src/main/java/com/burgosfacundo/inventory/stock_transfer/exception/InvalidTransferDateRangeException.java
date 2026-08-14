package com.burgosfacundo.inventory.stock_transfer.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class InvalidTransferDateRangeException extends BadRequestException {

    public InvalidTransferDateRangeException() {
        super(
                "'from' date cannot be after 'to' date",
                "INVALID_DATE_RANGE"
        );
    }
}