package com.burgosfacundo.inventory.stock.mapper;

import com.burgosfacundo.inventory.product.dto.ProductSummaryResponse;
import com.burgosfacundo.inventory.stock.dto.StockResponse;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseSummaryResponse;
import com.burgosfacundo.inventory.stock.model.Stock;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StockMapper {

    public static StockResponse toResponse(
            Stock stock
    ) {
        var product = stock.getProduct();
        var warehouse = stock.getWarehouse();

        ProductSummaryResponse productResponse =
                new ProductSummaryResponse(
                        product.getId(),
                        product.getSku(),
                        product.getName()
                );

        WarehouseSummaryResponse warehouseResponse =
                new WarehouseSummaryResponse(
                        warehouse.getId(),
                        warehouse.getCode(),
                        warehouse.getName()
                );

        return new StockResponse(
                stock.getId(),
                productResponse,
                warehouseResponse,
                stock.getQuantity(),
                stock.getMinimumStock(),
                stock.isLowStock()
        );
    }
}