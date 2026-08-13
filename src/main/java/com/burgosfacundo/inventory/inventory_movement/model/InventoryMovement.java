package com.burgosfacundo.inventory.inventory_movement.model;

import com.burgosfacundo.inventory.inventory_movement.exception.MovementQuantityInvalidException;
import com.burgosfacundo.inventory.inventory_movement.exception.MovementTypeRequiredException;
import com.burgosfacundo.inventory.inventory_movement.exception.ProductRequiredException;
import com.burgosfacundo.inventory.inventory_movement.exception.WarehouseRequiredException;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryMovement {

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

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 10
    )
    private MovementType type;

    @Column(nullable = false)
    private int quantity;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public InventoryMovement(Product product, Warehouse warehouse, MovementType type, int quantity) {
        validateProduct(product);
        validateWarehouse(warehouse);
        validateType(type);
        validateQuantity(quantity);

        this.product = product;
        this.warehouse = warehouse;
        this.type = type;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
    }

    private static void validateProduct(Product product) {
        if (product == null) {
            throw new ProductRequiredException();
        }
    }

    private static void validateWarehouse(Warehouse warehouse) {
        if (warehouse == null) {
            throw new WarehouseRequiredException();
        }
    }

    private static void validateType(MovementType type) {
        if (type == null) {
            throw new MovementTypeRequiredException();
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new MovementQuantityInvalidException();
        }
    }
}