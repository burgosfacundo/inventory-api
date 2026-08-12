package com.burgosfacundo.inventory.product_supplier.exception;

import com.burgosfacundo.inventory.common.exception.ConflictException;

public class ProductSupplierAlreadyExistsException
        extends ConflictException {

    public ProductSupplierAlreadyExistsException(Long productId, Long supplierId) {
        super(
                "Association between product " + productId +
                        " and supplier " + supplierId +
                        " already exists",

                "PRODUCT_SUPPLIER_ALREADY_EXISTS"
        );
    }
}
