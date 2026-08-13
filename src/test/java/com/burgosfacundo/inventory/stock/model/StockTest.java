package com.burgosfacundo.inventory.stock.model;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.stock.exception.MinimumStockInvalidException;
import com.burgosfacundo.inventory.stock.exception.ProductRequiredException;
import com.burgosfacundo.inventory.stock.exception.StockQuantityInvalidException;
import com.burgosfacundo.inventory.stock.exception.WarehouseRequiredException;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockTest {

    private Product product() {
        Category category =
                new Category(
                        "Category",
                        null
                );

        return new Product(
                "SKU-1",
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
                        new BigDecimal("-38.0055"),
                        new BigDecimal("-57.5426")
                );

        return new Warehouse(
                "WH-001",
                "Main Warehouse",
                address
        );
    }

    @Test
    void shouldCreateValidStock() {
        Stock stock =
                new Stock(
                        product(),
                        warehouse(),
                        10,
                        5
                );

        assertThat(stock.getProduct())
                .isNotNull();

        assertThat(stock.getWarehouse())
                .isNotNull();

        assertThat(stock.getQuantity())
                .isEqualTo(10);

        assertThat(stock.getMinimumStock())
                .isEqualTo(5);
    }

    @Test
    void shouldRejectNullProduct() {
        assertThrows(
                ProductRequiredException.class,
                () -> new Stock(
                        null,
                        warehouse(),
                        10,
                        5
                )
        );
    }

    @Test
    void shouldRejectNullWarehouse() {
        assertThrows(
                WarehouseRequiredException.class,
                () -> new Stock(
                        product(),
                        null,
                        10,
                        5
                )
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(
                StockQuantityInvalidException.class,
                () -> new Stock(
                        product(),
                        warehouse(),
                        -1,
                        5
                )
        );
    }

    @Test
    void shouldRejectNegativeMinimumStock() {
        assertThrows(
                MinimumStockInvalidException.class,
                () -> new Stock(
                        product(),
                        warehouse(),
                        10,
                        -1
                )
        );
    }

    @Test
    void shouldUpdateMinimumStock() {
        Stock stock =
                new Stock(
                        product(),
                        warehouse(),
                        10,
                        5
                );

        stock.updateMinimumStock(20);

        assertThat(stock.getMinimumStock())
                .isEqualTo(20);

        assertThat(stock.getQuantity())
                .isEqualTo(10);
    }

    @Test
    void shouldRejectNegativeMinimumStockWhenUpdating() {
        Stock stock =
                new Stock(
                        product(),
                        warehouse(),
                        10,
                        5
                );

        assertThrows(
                MinimumStockInvalidException.class,
                () -> stock.updateMinimumStock(-1)
        );

        assertThat(stock.getMinimumStock())
                .isEqualTo(5);
    }
}