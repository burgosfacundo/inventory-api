package com.burgosfacundo.inventory.product.exception;

import com.burgosfacundo.inventory.common.exception.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(Long id) {
        super(
                "Product not found with id: " + id,
                "PRODUCT_NOT_FOUND"
        );
    }
}
