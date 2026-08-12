package com.burgosfacundo.inventory.warehouse.exception;

import com.burgosfacundo.inventory.common.exception.UnprocessableContentException;

public class AddressNotFoundException
        extends UnprocessableContentException {

    public AddressNotFoundException() {
        super(
                "Address could not be resolved",
                "ADDRESS_NOT_FOUND"
        );
    }
}
