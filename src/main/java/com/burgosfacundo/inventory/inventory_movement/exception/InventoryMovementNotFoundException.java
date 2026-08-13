package com.burgosfacundo.inventory.inventory_movement.exception;

import com.burgosfacundo.inventory.common.exception.ResourceNotFoundException;

public class InventoryMovementNotFoundException extends ResourceNotFoundException {

    public InventoryMovementNotFoundException(Long id) {
        super(
                "Inventory movement not found with id: " + id,
                "MOVEMENT_NOT_FOUND"
        );
    }
}