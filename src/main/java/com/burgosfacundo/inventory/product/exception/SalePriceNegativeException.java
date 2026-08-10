package com.burgosfacundo.inventory.product.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class SalePriceNegativeException extends BadRequestException {
    public SalePriceNegativeException() {
        super("Sale price cannot be negative",
                "SALE_PRICE_NEGATIVE");
    }
}
