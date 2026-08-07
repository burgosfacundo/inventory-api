package com.burgosfacundo.inventory.common.exception;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}
