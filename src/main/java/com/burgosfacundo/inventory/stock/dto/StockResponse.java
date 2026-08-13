package com.burgosfacundo.inventory.stock.dto;

import com.burgosfacundo.inventory.product.dto.ProductSummaryResponse;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseSummaryResponse;

public record StockResponse(
        Long id,
        ProductSummaryResponse product,
        WarehouseSummaryResponse warehouse,
        int quantity,
        int minimumStock,
        boolean lowStock
) {
}