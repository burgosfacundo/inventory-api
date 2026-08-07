package com.burgosfacundo.inventory.common.exception;

import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {

    private final String errorCode;

    protected ApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}