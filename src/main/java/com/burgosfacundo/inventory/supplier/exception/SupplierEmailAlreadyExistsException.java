package com.burgosfacundo.inventory.supplier.exception;

import com.burgosfacundo.inventory.common.exception.ConflictException;

public class SupplierEmailAlreadyExistsException extends ConflictException {
    public SupplierEmailAlreadyExistsException(String email) {
        super(
                "Supplier with email '" + email + "' already exists.",
                "SUPPLIER_EMAIL_ALREADY_EXISTS");
    }
}
