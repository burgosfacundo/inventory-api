package com.burgosfacundo.inventory.product.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class SkuRequiredException extends BadRequestException {
    public SkuRequiredException() {
        super(
                "SKU is required",
                "SKU_REQUIRED"
        );
    }
}
