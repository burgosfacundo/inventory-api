package com.burgosfacundo.inventory.supplier.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class EmailRequiredException extends BadRequestException {
    public EmailRequiredException(){
        super("Email is required","EMAIL_REQUIRED");
    }
}
