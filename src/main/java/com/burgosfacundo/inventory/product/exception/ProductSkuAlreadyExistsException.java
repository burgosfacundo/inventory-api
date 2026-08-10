package com.burgosfacundo.inventory.product.exception;

import com.burgosfacundo.inventory.common.exception.ConflictException;

public class ProductSkuAlreadyExistsException extends ConflictException {
    public ProductSkuAlreadyExistsException(String sku) {
        super(
                "Product with SKU '" + sku + "' already exists.",
                "PRODUCT_SKU_ALREADY_EXISTS");
    }
}
