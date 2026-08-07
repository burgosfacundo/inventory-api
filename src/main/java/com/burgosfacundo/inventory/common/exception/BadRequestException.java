package com.burgosfacundo.inventory.common.exception;

public class BadRequestException extends ApiException {
    public BadRequestException(String message, String errorCode) {
        super(message, errorCode);
    }
}
