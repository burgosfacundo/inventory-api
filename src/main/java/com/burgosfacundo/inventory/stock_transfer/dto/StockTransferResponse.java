package com.burgosfacundo.inventory.stock_transfer.dto;

import com.burgosfacundo.inventory.product.dto.ProductSummaryResponse;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseSummaryResponse;

import java.time.LocalDateTime;

public record StockTransferResponse(
        Long id,
        ProductSummaryResponse product,
        WarehouseSummaryResponse sourceWarehouse,
        WarehouseSummaryResponse destinationWarehouse,
        int quantity,
        LocalDateTime createdAt
) {
}