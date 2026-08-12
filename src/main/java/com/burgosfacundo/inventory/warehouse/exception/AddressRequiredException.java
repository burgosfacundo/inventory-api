package com.burgosfacundo.inventory.warehouse.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class AddressRequiredException extends BadRequestException {
    public AddressRequiredException() {
        super("Address is required",
                "ADDRESS_REQUIRED");
    }
}
