package com.burgosfacundo.inventory.category.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class NameRequiredException extends BadRequestException {
    public NameRequiredException() {
        super(
                "Name is required",
                "NAME_REQUIRED"
        );
    }
}
