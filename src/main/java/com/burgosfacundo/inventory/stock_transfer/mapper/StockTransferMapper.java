package com.burgosfacundo.inventory.stock_transfer.mapper;

import com.burgosfacundo.inventory.product.dto.ProductSummaryResponse;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.stock_transfer.dto.StockTransferResponse;
import com.burgosfacundo.inventory.stock_transfer.model.StockTransfer;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseSummaryResponse;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StockTransferMapper {

    public static StockTransfer toEntity(Product product,Warehouse source, Warehouse destination, int quantity) {
        return new StockTransfer(product,source,destination,quantity);
    }

    public static StockTransferResponse toResponse(StockTransfer transfer) {
        Product product = transfer.getProduct();

        Warehouse sourceWarehouse = transfer.getSourceWarehouse();

        Warehouse destinationWarehouse = transfer.getDestinationWarehouse();

        ProductSummaryResponse productResponse = new ProductSummaryResponse(product.getId(), product.getSku(), product.getName());

        WarehouseSummaryResponse sourceResponse = new WarehouseSummaryResponse(sourceWarehouse.getId(), sourceWarehouse.getCode(), sourceWarehouse.getName());

        WarehouseSummaryResponse destinationResponse = new WarehouseSummaryResponse(destinationWarehouse.getId(), destinationWarehouse.getCode(), destinationWarehouse.getName());

        return new StockTransferResponse(transfer.getId(), productResponse, sourceResponse,
                destinationResponse, transfer.getQuantity(), transfer.getCreatedAt());
    }
}