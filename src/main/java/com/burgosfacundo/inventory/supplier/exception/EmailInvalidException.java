package com.burgosfacundo.inventory.supplier.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class EmailInvalidException extends BadRequestException {
    public EmailInvalidException() {
        super("Email format is invalid","EMAIL_FORMAT_INVALID");
    }
}
