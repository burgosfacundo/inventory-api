package com.burgosfacundo.inventory.stock_transfer.model;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.stock_transfer.exception.DestinationWarehouseRequiredException;
import com.burgosfacundo.inventory.stock_transfer.exception.ProductRequiredException;
import com.burgosfacundo.inventory.stock_transfer.exception.SameWarehouseTransferException;
import com.burgosfacundo.inventory.stock_transfer.exception.SourceWarehouseRequiredException;
import com.burgosfacundo.inventory.stock_transfer.exception.TransferQuantityInvalidException;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockTransferTest {

    private Product product() {
        Category category = new Category("Category", null);

        return new Product("SKU-001", "Product", null,new BigDecimal("100.00"),category);
    }

    private Warehouse warehouse(Long id, String code) {
        Address address = new Address("Avenida Independencia", "1234", "B7600",
                "Mar del Plata", "Buenos Aires", "AR",
                new BigDecimal("-38.0055000"), new BigDecimal("-57.5426000"));

        Warehouse warehouse = new Warehouse(code, "Warehouse " + code, address);

        if (id != null) {
            ReflectionTestUtils.setField(warehouse,"id", id);
        }

        return warehouse;
    }

    @Test
    void shouldCreateStockTransfer() {
        Product product = product();

        Warehouse source = warehouse(1L, "WH-001");

        Warehouse destination = warehouse(2L, "WH-002");

        StockTransfer transfer = new StockTransfer(product, source, destination, 10);

        assertThat(transfer.getProduct()).isSameAs(product);
        assertThat(transfer.getSourceWarehouse()).isSameAs(source);
        assertThat(transfer.getDestinationWarehouse()).isSameAs(destination);
        assertThat(transfer.getQuantity()).isEqualTo(10);
        assertThat(transfer.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectNullProduct() {
        assertThrows(ProductRequiredException.class,
                () -> new StockTransfer(null, warehouse(1L, "WH-001"),
                        warehouse(2L, "WH-002"), 10)
        );
    }

    @Test
    void shouldRejectNullSourceWarehouse() {
        assertThrows(SourceWarehouseRequiredException.class,
                () -> new StockTransfer(product(), null,
                        warehouse(2L, "WH-002"), 10)
        );
    }

    @Test
    void shouldRejectNullDestinationWarehouse() {
        assertThrows(DestinationWarehouseRequiredException.class,
                () -> new StockTransfer(product(), warehouse(1L, "WH-001"),
                        null, 10)
        );
    }

    @Test
    void shouldRejectSameWarehouseInstance() {
        Warehouse warehouse = warehouse(1L, "WH-001");

        assertThrows(SameWarehouseTransferException.class,
                () -> new StockTransfer(product(), warehouse, warehouse, 10)
        );
    }

    @Test
    void shouldRejectDifferentWarehouseInstancesWithSameId() {
        Warehouse source = warehouse(1L, "WH-001");

        Warehouse destination = warehouse(1L, "WH-002");

        assertThrows(SameWarehouseTransferException.class,
                () -> new StockTransfer(product(), source, destination, 10)
        );
    }

    @Test
    void shouldAllowDifferentWarehouses() {
        StockTransfer transfer = new StockTransfer(product(), warehouse(1L, "WH-001"),
                        warehouse(2L, "WH-002"), 10);

        assertThat(transfer.getSourceWarehouse().getId()).isNotEqualTo(transfer.getDestinationWarehouse().getId());
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(TransferQuantityInvalidException.class,
                () -> new StockTransfer(product(), warehouse(1L, "WH-001"),
                        warehouse(2L, "WH-002"), 0)
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(TransferQuantityInvalidException.class,
                () -> new StockTransfer(product(), warehouse(1L, "WH-001"),
                        warehouse(2L, "WH-002"), -1)
        );
    }
}