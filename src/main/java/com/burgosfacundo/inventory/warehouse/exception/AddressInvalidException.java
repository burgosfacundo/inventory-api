package com.burgosfacundo.inventory.warehouse.exception;

import com.burgosfacundo.inventory.common.exception.UnprocessableContentException;

public class AddressInvalidException
        extends UnprocessableContentException {

    public AddressInvalidException() {
        super(
                "Address could not be validated",
                "ADDRESS_INVALID"
        );
    }
}