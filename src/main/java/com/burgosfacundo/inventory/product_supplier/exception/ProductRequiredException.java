package com.burgosfacundo.inventory.product_supplier.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class ProductRequiredException extends BadRequestException {
    public ProductRequiredException() {
        super("Product is required",
                "PRODUCT_REQUIRED");
    }
}
