package com.burgosfacundo.inventory.inventory_movement.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class MovementTypeRequiredException
        extends BadRequestException {

    public MovementTypeRequiredException() {
        super(
                "Movement type is required",
                "MOVEMENT_TYPE_REQUIRED"
        );
    }
}
