package com.burgosfacundo.inventory.stock.model;

import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.stock.exception.ProductRequiredException;
import com.burgosfacundo.inventory.stock.exception.StockQuantityInvalidException;
import com.burgosfacundo.inventory.stock.exception.WarehouseRequiredException;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "stocks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_stocks_product_warehouse",
                columnNames = {
                        "product_id",
                        "warehouse_id"
                }
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED) @Getter
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "warehouse_id",
            nullable = false
    )
    private Warehouse warehouse;

    @Column(nullable = false)
    private int quantity;

    public Stock(
            Product product,
            Warehouse warehouse,
            int quantity
    ) {
        validateProduct(product);
        validateWarehouse(warehouse);
        validateQuantity(quantity);

        this.product = product;
        this.warehouse = warehouse;
        this.quantity = quantity;
    }

    private static void validateProduct(Product product) {
        if (product == null) {
            throw new ProductRequiredException();
        }
    }

    private static void validateWarehouse(
            Warehouse warehouse
    ) {
        if (warehouse == null) {
            throw new WarehouseRequiredException();
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new StockQuantityInvalidException();
        }
    }
}