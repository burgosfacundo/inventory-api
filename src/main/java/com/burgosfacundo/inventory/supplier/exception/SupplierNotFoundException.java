package com.burgosfacundo.inventory.supplier.exception;

import com.burgosfacundo.inventory.common.exception.ResourceNotFoundException;

public class SupplierNotFoundException extends ResourceNotFoundException {
    public SupplierNotFoundException(Long id) {
        super( "Supplier not found with id: " + id,
                "SUPPLIER_NOT_FOUND");
    }
}
