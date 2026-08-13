package com.burgosfacundo.inventory.inventory_movement.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class MovementQuantityInvalidException
        extends BadRequestException {

    public MovementQuantityInvalidException() {
        super(
                "Movement quantity must be greater than zero",
                "INVALID_MOVEMENT_QUANTITY"
        );
    }
}