package com.burgosfacundo.inventory.product_supplier.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class InvalidPurchasePriceException extends BadRequestException {
    public InvalidPurchasePriceException() {
        super("Purchase price cannot be negative",
                "PURCHASE_PRICE_INVALID");
    }
}
