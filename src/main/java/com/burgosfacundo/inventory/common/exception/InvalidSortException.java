package com.burgosfacundo.inventory.common.exception;

public class InvalidSortException extends BadRequestException {
    public InvalidSortException(String message) {
        super(message, "INVALID_SORT");
    }
}
