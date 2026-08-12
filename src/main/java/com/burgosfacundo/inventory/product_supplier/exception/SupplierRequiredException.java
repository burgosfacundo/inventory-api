package com.burgosfacundo.inventory.product_supplier.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class SupplierRequiredException extends BadRequestException {
    public SupplierRequiredException() {
        super("Supplier is required", "SUPPLIER_REQUIRED");
    }
}
