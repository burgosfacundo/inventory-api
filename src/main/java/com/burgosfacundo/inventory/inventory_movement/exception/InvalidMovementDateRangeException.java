package com.burgosfacundo.inventory.inventory_movement.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class InvalidMovementDateRangeException
        extends BadRequestException {

    public InvalidMovementDateRangeException() {
        super(
                "'from' date cannot be after 'to' date",
                "INVALID_DATE_RANGE"
        );
    }
}