package com.burgosfacundo.inventory.inventory_movement.model;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.inventory_movement.exception.MovementQuantityInvalidException;
import com.burgosfacundo.inventory.inventory_movement.exception.MovementTypeRequiredException;
import com.burgosfacundo.inventory.inventory_movement.exception.ProductRequiredException;
import com.burgosfacundo.inventory.inventory_movement.exception.WarehouseRequiredException;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryMovementTest {

    private Product product() {
        Category category =
                new Category(
                        "Category",
                        null
                );

        return new Product(
                "SKU-001",
                "Product",
                null,
                new BigDecimal("100.00"),
                category
        );
    }

    private Warehouse warehouse() {
        Address address =
                new Address(
                        "Avenida Independencia",
                        "1234",
                        "B7600",
                        "Mar del Plata",
                        "Buenos Aires",
                        "AR",
                        new BigDecimal("-38.0055000"),
                        new BigDecimal("-57.5426000")
                );

        return new Warehouse(
                "WH-001",
                "Main Warehouse",
                address
        );
    }

    @Test
    void shouldCreateInMovement() {
        InventoryMovement movement =
                new InventoryMovement(
                        product(),
                        warehouse(),
                        MovementType.IN,
                        10
                );

        assertThat(movement.getProduct())
                .isNotNull();

        assertThat(movement.getWarehouse())
                .isNotNull();

        assertThat(movement.getType())
                .isEqualTo(MovementType.IN);

        assertThat(movement.getQuantity())
                .isEqualTo(10);

        assertThat(movement.getCreatedAt())
                .isNotNull();
    }

    @Test
    void shouldCreateOutMovement() {
        InventoryMovement movement =
                new InventoryMovement(
                        product(),
                        warehouse(),
                        MovementType.OUT,
                        5
                );

        assertThat(movement.getType())
                .isEqualTo(MovementType.OUT);

        assertThat(movement.getQuantity())
                .isEqualTo(5);
    }

    @Test
    void shouldRejectNullProduct() {
        assertThrows(
                ProductRequiredException.class,
                () -> new InventoryMovement(
                        null,
                        warehouse(),
                        MovementType.IN,
                        10
                )
        );
    }

    @Test
    void shouldRejectNullWarehouse() {
        assertThrows(
                WarehouseRequiredException.class,
                () -> new InventoryMovement(
                        product(),
                        null,
                        MovementType.IN,
                        10
                )
        );
    }

    @Test
    void shouldRejectNullMovementType() {
        assertThrows(
                MovementTypeRequiredException.class,
                () -> new InventoryMovement(
                        product(),
                        warehouse(),
                        null,
                        10
                )
        );
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(
                MovementQuantityInvalidException.class,
                () -> new InventoryMovement(
                        product(),
                        warehouse(),
                        MovementType.IN,
                        0
                )
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(
                MovementQuantityInvalidException.class,
                () -> new InventoryMovement(
                        product(),
                        warehouse(),
                        MovementType.OUT,
                        -1
                )
        );
    }
}