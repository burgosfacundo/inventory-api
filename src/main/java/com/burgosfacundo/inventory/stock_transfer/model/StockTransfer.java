package com.burgosfacundo.inventory.stock_transfer.model;

import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.stock_transfer.exception.*;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity @Table(name = "stock_transfers")
@NoArgsConstructor(access = AccessLevel.PROTECTED) @Getter
public class StockTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_warehouse_id")
    private Warehouse sourceWarehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_warehouse_id")
    private Warehouse destinationWarehouse;

    @Column(nullable = false)
    int quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public StockTransfer(Product product, Warehouse sourceWarehouse, Warehouse destinationWarehouse, int quantity) {
        validateProduct(product);
        validateSourceWarehouse(sourceWarehouse);
        validateDestinationWarehouse(destinationWarehouse);
        validateDifferentWarehouses(sourceWarehouse, destinationWarehouse);
        validateQuantity(quantity);

        this.product = product;
        this.sourceWarehouse = sourceWarehouse;
        this.destinationWarehouse = destinationWarehouse;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
    }

    private void validateProduct(Product product) {
        if(product == null) {
            throw new ProductRequiredException();
        }
    }

    private void validateSourceWarehouse(Warehouse sourceWarehouse) {
        if(sourceWarehouse == null) {
            throw new SourceWarehouseRequiredException();
        }
    }

    private void validateDestinationWarehouse(Warehouse destinationWarehouse) {
        if(destinationWarehouse == null) {
            throw new DestinationWarehouseRequiredException();
        }
    }

    private static void validateDifferentWarehouses(Warehouse source, Warehouse destination) {
        boolean sameInstance = source == destination;

        boolean samePersistedWarehouse = source.getId() != null && source.getId().equals(destination.getId());

        if (sameInstance || samePersistedWarehouse) {
            throw new SameWarehouseTransferException();
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new TransferQuantityInvalidException();
        }
    }
}
