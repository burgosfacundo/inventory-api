package com.burgosfacundo.inventory.common.exception;

public abstract class ServiceUnavailableException
        extends ApiException {

    protected ServiceUnavailableException(
            String message,
            String errorCode
    ) {
        super(message, errorCode);
    }
}