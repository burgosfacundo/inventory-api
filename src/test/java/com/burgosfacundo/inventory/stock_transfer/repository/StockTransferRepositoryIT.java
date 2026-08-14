package com.burgosfacundo.inventory.stock_transfer.repository;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.category.repository.CategoryRepository;
import com.burgosfacundo.inventory.config.IntegrationTest;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product.repository.ProductRepository;
import com.burgosfacundo.inventory.stock_transfer.model.StockTransfer;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import com.burgosfacundo.inventory.warehouse.repository.WarehouseRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class StockTransferRepositoryIT
        extends IntegrationTest {

    @Autowired
    private StockTransferRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Category createCategory() {
        return categoryRepository.saveAndFlush(new Category("Category", null));
    }

    private Product createProduct(String sku, Category category) {
        return productRepository.saveAndFlush(new Product(sku, "Product " + sku, null,
                        new BigDecimal("100.00"), category));
    }

    private Warehouse createWarehouse(String code) {
        Address address = new Address("Avenida Independencia", "1234", "B7600",
                        "Mar del Plata", "Buenos Aires", "AR",
                        new BigDecimal("-38.0055000"), new BigDecimal("-57.5426000"));

        return warehouseRepository.saveAndFlush(new Warehouse(code, "Warehouse " + code, address));
    }

    @Test
    void shouldPersistAndRetrieveStockTransfer() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse source = createWarehouse("WH-001");

        Warehouse destination = createWarehouse("WH-002");

        StockTransfer transfer = repository.saveAndFlush(new StockTransfer(product, source, destination, 10));

        Long transferId = transfer.getId();

        entityManager.clear();

        StockTransfer found = repository.findWithRelationsById(transferId).orElseThrow();

        assertThat(found.getId()).isEqualTo(transferId);
        assertThat(found.getProduct().getId()).isEqualTo(product.getId());
        assertThat(found.getSourceWarehouse().getId()).isEqualTo(source.getId());
        assertThat(found.getDestinationWarehouse().getId()).isEqualTo(destination.getId());
        assertThat(found.getQuantity()).isEqualTo(10);
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldLoadRelationsWithEntityGraph() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse source = createWarehouse("WH-001");

        Warehouse destination = createWarehouse("WH-002");

        StockTransfer transfer = repository.saveAndFlush(new StockTransfer(product, source, destination, 10));

        Long transferId = transfer.getId();

        entityManager.clear();

        StockTransfer found = repository.findWithRelationsById(transferId).orElseThrow();

        assertThat(Hibernate.isInitialized(found.getProduct())).isTrue();

        assertThat(Hibernate.isInitialized(found.getSourceWarehouse())).isTrue();
        assertThat(Hibernate.isInitialized(found.getDestinationWarehouse())).isTrue();
    }

    @Test
    void shouldFilterStockTransfers() {
        Category category = createCategory();

        Product product1 = createProduct("SKU-001", category);

        Product product2 = createProduct("SKU-002", category);

        Warehouse warehouse1 = createWarehouse("WH-001");

        Warehouse warehouse2 = createWarehouse("WH-002");

        Warehouse warehouse3 = createWarehouse("WH-003");

        StockTransfer transfer1 = new StockTransfer(product1, warehouse1, warehouse2, 10);

        StockTransfer transfer2 = new StockTransfer(product1, warehouse2, warehouse3, 5);

        StockTransfer transfer3 = new StockTransfer(product2, warehouse1, warehouse3, 20);

        repository.saveAllAndFlush(List.of(transfer1, transfer2, transfer3));

        Pageable pageable = PageRequest.of(0, 20);

        var all = repository.findAllFiltered(null, null,
                null, null, null, pageable);

        var byProduct = repository.findAllFiltered(product1.getId(), null, null,
                        null, null, pageable);

        var bySource = repository.findAllFiltered(null, warehouse1.getId(), null,
                        null, null, pageable);

        var byDestination = repository.findAllFiltered(null, null, warehouse3.getId(),
                        null, null, pageable);

        var combined = repository.findAllFiltered(product1.getId(), warehouse1.getId(), warehouse2.getId(),
                        null, null, pageable);

        assertThat(all.getContent()).hasSize(3);
        assertThat(byProduct.getContent()).hasSize(2)
                .allMatch(transfer -> transfer.getProduct()
                        .getId().equals(product1.getId()));
        assertThat(bySource.getContent()).hasSize(2)
                .allMatch(transfer -> transfer.getSourceWarehouse()
                        .getId().equals(warehouse1.getId()));
        assertThat(byDestination.getContent()).hasSize(2)
                .allMatch(transfer -> transfer.getDestinationWarehouse()
                        .getId().equals(warehouse3.getId()));
        assertThat(combined.getContent()).hasSize(1);

        StockTransfer result = combined.getContent().getFirst();

        assertThat(result.getProduct().getId()).isEqualTo(product1.getId());
        assertThat(result.getSourceWarehouse().getId()).isEqualTo(warehouse1.getId());
        assertThat(result.getDestinationWarehouse().getId()).isEqualTo(warehouse2.getId());
    }

    @Test
    void shouldFilterStockTransfersByDateRange() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse source = createWarehouse("WH-001");

        Warehouse destination = createWarehouse("WH-002");

        StockTransfer transfer = repository.saveAndFlush(new StockTransfer(product, source, destination, 10));

        LocalDateTime from = transfer.getCreatedAt().minusSeconds(1);

        LocalDateTime to = transfer.getCreatedAt().plusSeconds(1);

        Pageable pageable = PageRequest.of(0, 20);

        var insideRange = repository.findAllFiltered(null, null, null,
                        from, to, pageable);

        var afterTransfer = repository.findAllFiltered(null, null, null,
                        transfer.getCreatedAt().plusMinutes(1), null, pageable);

        var beforeTransfer = repository.findAllFiltered(null, null, null,
                        null, transfer.getCreatedAt().minusMinutes(1), pageable);

        assertThat(insideRange.getContent()).hasSize(1);
        assertThat(afterTransfer.getContent()).isEmpty();
        assertThat(beforeTransfer.getContent()).isEmpty();
    }

    @Test
    void shouldRejectZeroQuantityAtDatabaseLevel() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse source = createWarehouse("WH-001");

        Warehouse destination = createWarehouse("WH-002");

        var exception = assertThrows(DataAccessException.class,
                () -> jdbcTemplate.update(
                                """
                                INSERT INTO stock_transfers (
                                    product_id,
                                    source_warehouse_id,
                                    destination_warehouse_id,
                                    quantity,
                                    created_at
                                )
                                VALUES (?, ?, ?, ?, ?)
                                """,
                                product.getId(),
                                source.getId(),
                                destination.getId(),
                                0,
                                LocalDateTime.now()
                        )
                );

        assertThat(exception.getMostSpecificCause().getMessage()).contains("chk_stock_transfers_quantity");
    }
    @Test
    void shouldRejectSameWarehouseAtDatabaseLevel() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse warehouse = createWarehouse("WH-001");

        var exception = assertThrows(DataAccessException.class,
                () -> jdbcTemplate.update(
                                """
                                INSERT INTO stock_transfers (
                                    product_id,
                                    source_warehouse_id,
                                    destination_warehouse_id,
                                    quantity,
                                    created_at
                                )
                                VALUES (?, ?, ?, ?, ?)
                                """,
                                product.getId(),
                                warehouse.getId(),
                                warehouse.getId(),
                                10,
                                LocalDateTime.now()
                        )
                );

        assertThat(exception.getMostSpecificCause().getMessage()).contains("chk_stock_transfers_different_warehouses");
    }

    @Test
    void shouldRejectNonExistingProductAtDatabaseLevel() {
        Warehouse source = createWarehouse("WH-001");

        Warehouse destination = createWarehouse("WH-002");

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO stock_transfers (
                            product_id,
                            source_warehouse_id,
                            destination_warehouse_id,
                            quantity,
                            created_at
                        )
                        VALUES (?, ?, ?, ?, ?)
                        """,
                        Long.MAX_VALUE,
                        source.getId(),
                        destination.getId(),
                        10,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void shouldRejectNonExistingSourceWarehouseAtDatabaseLevel() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse destination = createWarehouse("WH-002");

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO stock_transfers (
                            product_id,
                            source_warehouse_id,
                            destination_warehouse_id,
                            quantity,
                            created_at
                        )
                        VALUES (?, ?, ?, ?, ?)
                        """,
                        product.getId(),
                        Long.MAX_VALUE,
                        destination.getId(),
                        10,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void shouldRejectNonExistingDestinationWarehouseAtDatabaseLevel() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse source = createWarehouse("WH-001");

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO stock_transfers (
                            product_id,
                            source_warehouse_id,
                            destination_warehouse_id,
                            quantity,
                            created_at
                        )
                        VALUES (?, ?, ?, ?, ?)
                        """,
                        product.getId(),
                        source.getId(),
                        Long.MAX_VALUE,
                        10,
                        LocalDateTime.now()
                )
        );
    }
}