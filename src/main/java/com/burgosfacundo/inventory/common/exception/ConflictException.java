package com.burgosfacundo.inventory.common.exception;

public class ConflictException extends ApiException {

    public ConflictException(String message, String errorCode) {
        super(message, errorCode);
    }
}