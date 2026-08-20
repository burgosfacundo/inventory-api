package com.burgosfacundo.inventory.inventory_movement.repository;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.category.repository.CategoryRepository;
import com.burgosfacundo.inventory.config.IntegrationTest;
import com.burgosfacundo.inventory.inventory_movement.model.InventoryMovement;
import com.burgosfacundo.inventory.inventory_movement.model.MovementType;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product.repository.ProductRepository;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import com.burgosfacundo.inventory.warehouse.repository.WarehouseRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
class InventoryMovementRepositoryIT
        extends IntegrationTest {

    @Autowired
    private InventoryMovementRepository repository;

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
    void shouldPersistAndRetrieveInMovement() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse warehouse = createWarehouse("WH-001");

        InventoryMovement movement = repository.saveAndFlush(new InventoryMovement(product, warehouse,
                        MovementType.IN, 10));

        entityManager.clear();

        InventoryMovement found = repository.findWithRelationsById(movement.getId())
                .orElseThrow();

        assertThat(found.getId()).isEqualTo(movement.getId());
        assertThat(found.getProduct().getId()).isEqualTo(product.getId());
        assertThat(found.getWarehouse().getId()).isEqualTo(warehouse.getId());
        assertThat(found.getType()).isEqualTo(MovementType.IN);
        assertThat(found.getQuantity()).isEqualTo(10);
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldPersistOutMovement() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse warehouse = createWarehouse("WH-001");

        InventoryMovement movement = repository.saveAndFlush(new InventoryMovement(product, warehouse,
                        MovementType.OUT, 5));

        entityManager.clear();

        InventoryMovement found = repository.findWithRelationsById(movement.getId())
                .orElseThrow();

        assertThat(found.getType()).isEqualTo(MovementType.OUT);
        assertThat(found.getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldLoadProductAndWarehouseWithEntityGraph() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse warehouse = createWarehouse("WH-001");

        InventoryMovement movement = repository.saveAndFlush(new InventoryMovement(product, warehouse,
                        MovementType.IN, 10));

        entityManager.clear();

        InventoryMovement found = repository.findWithRelationsById(movement.getId())
                .orElseThrow();

        assertThat(Hibernate.isInitialized(found.getProduct())).isTrue();
        assertThat(Hibernate.isInitialized(found.getWarehouse())).isTrue();
    }

    @Test
    void shouldFilterMovements() {
        Category category = createCategory();

        Product product1 = createProduct("SKU-001", category);

        Product product2 = createProduct("SKU-002", category);

        Warehouse warehouse1 = createWarehouse("WH-001");

        Warehouse warehouse2 = createWarehouse("WH-002");

        InventoryMovement movement1 = new InventoryMovement(product1, warehouse1, MovementType.IN, 10);

        InventoryMovement movement2 = new InventoryMovement(product1, warehouse2, MovementType.OUT, 4);

        InventoryMovement movement3 = new InventoryMovement(product2, warehouse1, MovementType.IN, 20);

        repository.saveAllAndFlush(List.of(movement1, movement2, movement3));

        Pageable pageable = PageRequest.of(0, 20);

        var all = repository.findAllFiltered(null, null, null,
                null, null, pageable);

        var byProduct = repository.findAllFiltered(product1.getId(), null, null,
                        null, null, pageable);

        var byWarehouse = repository.findAllFiltered(null, warehouse1.getId(), null,
                        null, null, pageable);

        var byType = repository.findAllFiltered(null, null, MovementType.OUT,
                        null, null, pageable);

        var combined = repository.findAllFiltered(product1.getId(), warehouse1.getId(), MovementType.IN,
                        null, null, pageable);

        assertThat(all.getContent()).hasSize(3);
        assertThat(byProduct.getContent()).hasSize(2)
                .allMatch(movement -> movement
                        .getProduct()
                        .getId()
                        .equals(product1.getId()));
        assertThat(byWarehouse.getContent()).hasSize(2)
                .allMatch(movement -> movement
                        .getWarehouse()
                        .getId()
                        .equals(warehouse1.getId())
                );
        assertThat(byType.getContent()).hasSize(1);
        assertThat(byType.getContent().getFirst().getType()).isEqualTo(MovementType.OUT);
        assertThat(combined.getContent()).hasSize(1);
        assertThat(combined.getContent().getFirst().getProduct().getId()).isEqualTo(product1.getId());

        assertThat(combined.getContent().getFirst().getWarehouse().getId()).isEqualTo(warehouse1.getId());
    }

    @Test
    void shouldFilterMovementsByDateRange() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse warehouse = createWarehouse("WH-001");

        InventoryMovement movement = repository.saveAndFlush(new InventoryMovement(product, warehouse,
                        MovementType.IN, 10));

        LocalDateTime from = movement.getCreatedAt().minusSeconds(1);

        LocalDateTime to = movement.getCreatedAt().plusSeconds(1);

        Pageable pageable = PageRequest.of(0, 20);

        var insideRange = repository.findAllFiltered(null, null, null, from,
                        to, pageable);

        var afterMovement = repository.findAllFiltered(null, null, null,
                movement.getCreatedAt().plusMinutes(1), null, pageable);

        var beforeMovement = repository.findAllFiltered(null, null, null,
                null, movement.getCreatedAt().minusMinutes(1), pageable);

        assertThat(insideRange.getContent()).hasSize(1);
        assertThat(afterMovement.getContent()).isEmpty();
        assertThat(beforeMovement.getContent()).isEmpty();
    }

    @Test
    void shouldRejectZeroQuantityAtDatabaseLevel() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse warehouse = createWarehouse("WH-001");

        assertThrows(DataAccessException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO inventory_movements
                            (
                                product_id,
                                warehouse_id,
                                type,
                                quantity,
                                created_at
                            )
                        VALUES (?, ?, ?, ?, ?)
                        """,
                        product.getId(),
                        warehouse.getId(),
                        "IN",
                        0,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void shouldRejectInvalidMovementTypeAtDatabaseLevel() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse warehouse = createWarehouse("WH-001");

        assertThrows(DataAccessException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO inventory_movements
                            (
                                product_id,
                                warehouse_id,
                                type,
                                quantity,
                                created_at
                            )
                        VALUES (?, ?, ?, ?, ?)
                        """,
                        product.getId(),
                        warehouse.getId(),
                        "INVALID",
                        10,
                        LocalDateTime.now()
                )
        );
    }
}