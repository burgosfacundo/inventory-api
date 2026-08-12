package com.burgosfacundo.inventory.common.exception;

public abstract class UnprocessableContentException
        extends ApiException {

    protected UnprocessableContentException(
            String message,
            String errorCode
    ) {
        super(message, errorCode);
    }
}