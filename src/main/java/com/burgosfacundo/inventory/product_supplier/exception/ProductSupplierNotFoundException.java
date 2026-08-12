package com.burgosfacundo.inventory.product_supplier.exception;

import com.burgosfacundo.inventory.common.exception.ResourceNotFoundException;

public class ProductSupplierNotFoundException extends ResourceNotFoundException {
    public ProductSupplierNotFoundException(Long id) {
        super(
                "Product Supplier not found with id: " + id,
                "PRODUCT_SUPPLIER_NOT_FOUND"
        );
    }
}
