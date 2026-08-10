package com.burgosfacundo.inventory.product.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class SalePriceRequiredException extends BadRequestException {
    public SalePriceRequiredException() {
        super("Sale price is required",
                "SALE_PRICE_REQUIRED");
    }
}
