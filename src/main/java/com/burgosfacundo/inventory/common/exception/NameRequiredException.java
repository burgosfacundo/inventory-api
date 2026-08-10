package com.burgosfacundo.inventory.common.exception;

public class NameRequiredException extends BadRequestException {
    public NameRequiredException() {
        super(
                "Name is required",
                "NAME_REQUIRED"
        );
    }
}
