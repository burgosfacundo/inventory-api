package com.burgosfacundo.inventory.product.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class CategoryRequiredException extends BadRequestException {
    public CategoryRequiredException() {
        super("Category is required", "CATEGORY_REQUIRED");
    }
}
