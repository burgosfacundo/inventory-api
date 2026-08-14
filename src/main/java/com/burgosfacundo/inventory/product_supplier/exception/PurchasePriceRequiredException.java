package com.burgosfacundo.inventory.product_supplier.exception;

import com.burgosfacundo.inventory.common.exception.BadRequestException;

public class PurchasePriceRequiredException extends BadRequestException {
    public PurchasePriceRequiredException() {
        super("Purchase price is required",
                "PURCHASE_PRICE_REQUIRED");
    }
}
