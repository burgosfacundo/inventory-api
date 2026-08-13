package com.burgosfacundo.inventory.stock.repository;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.category.repository.CategoryRepository;
import com.burgosfacundo.inventory.config.IntegrationTest;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product.repository.ProductRepository;
import com.burgosfacundo.inventory.stock.model.Stock;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import com.burgosfacundo.inventory.warehouse.repository.WarehouseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
class StockRepositoryIT extends IntegrationTest {

    @Autowired
    private StockRepository repository;

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
        return categoryRepository.saveAndFlush(
                new Category("Category", null)
        );
    }

    private Product createProduct(
            String sku,
            Category category
    ) {
        return productRepository.saveAndFlush(
                new Product(
                        sku,
                        "Product " + sku,
                        null,
                        new BigDecimal("100.00"),
                        category
                )
        );
    }

    private Warehouse createWarehouse(String code) {
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

        return warehouseRepository.saveAndFlush(
                new Warehouse(
                        code,
                        "Warehouse " + code,
                        address
                )
        );
    }

    @Test
    void shouldPersistAndRetrieveStock() {
        Category category =
                createCategory();

        Product product =
                createProduct("SKU-1", category);

        Warehouse warehouse =
                createWarehouse("WH-001");

        Stock stock =
                repository.saveAndFlush(
                        new Stock(
                                product,
                                warehouse,
                                20,
                                5
                        )
                );

        entityManager.clear();

        Stock found =
                repository.findWithRelationsById(
                                stock.getId()
                        )
                        .orElseThrow();

        assertThat(found.getId())
                .isEqualTo(stock.getId());

        assertThat(found.getProduct().getId())
                .isEqualTo(product.getId());

        assertThat(found.getWarehouse().getId())
                .isEqualTo(warehouse.getId());

        assertThat(found.getQuantity())
                .isEqualTo(20);

        assertThat(found.getMinimumStock())
                .isEqualTo(5);
    }

    @Test
    void shouldLoadProductAndWarehouseWithEntityGraph() {
        Category category =
                createCategory();

        Product product =
                createProduct("SKU-1", category);

        Warehouse warehouse =
                createWarehouse("WH-001");

        Stock stock =
                repository.saveAndFlush(
                        new Stock(
                                product,
                                warehouse,
                                20,
                                5
                        )
                );

        entityManager.clear();

        Stock found =
                repository.findWithRelationsById(
                                stock.getId()
                        )
                        .orElseThrow();

        assertThat(
                Hibernate.isInitialized(
                        found.getProduct()
                )
        ).isTrue();

        assertThat(
                Hibernate.isInitialized(
                        found.getWarehouse()
                )
        ).isTrue();
    }

    @Test
    void shouldDetectExistingProductWarehouseStock() {
        Category category =
                createCategory();

        Product product =
                createProduct("SKU-1", category);

        Warehouse warehouse =
                createWarehouse("WH-001");

        repository.saveAndFlush(
                new Stock(
                        product,
                        warehouse,
                        20,
                        5
                )
        );

        assertThat(
                repository.existsByProductIdAndWarehouseId(
                        product.getId(),
                        warehouse.getId()
                )
        ).isTrue();

        assertThat(
                repository.existsByProductIdAndWarehouseId(
                        product.getId(),
                        999L
                )
        ).isFalse();
    }

    @Test
    void shouldRejectDuplicateProductWarehouseStock() {
        Category category =
                createCategory();

        Product product =
                createProduct("SKU-1", category);

        Warehouse warehouse =
                createWarehouse("WH-001");

        repository.saveAndFlush(
                new Stock(
                        product,
                        warehouse,
                        20,
                        5
                )
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(
                        new Stock(
                                product,
                                warehouse,
                                30,
                                10
                        )
                )
        );
    }

    @Test
    void shouldFilterStockByProductAndWarehouse() {
        Category category =
                createCategory();

        Product product1 =
                createProduct("SKU-1", category);

        Product product2 =
                createProduct("SKU-2", category);

        Warehouse warehouse1 =
                createWarehouse("WH-001");

        Warehouse warehouse2 =
                createWarehouse("WH-002");

        repository.saveAllAndFlush(
                List.of(
                        new Stock(
                                product1,
                                warehouse1,
                                10,
                                5
                        ),
                        new Stock(
                                product1,
                                warehouse2,
                                20,
                                5
                        ),
                        new Stock(
                                product2,
                                warehouse1,
                                30,
                                5
                        )
                )
        );

        Pageable pageable =
                PageRequest.of(0, 20);

        var all =
                repository.findAllFiltered(
                        null,
                        null,
                        pageable
                );

        var byProduct =
                repository.findAllFiltered(
                        product1.getId(),
                        null,
                        pageable
                );

        var byWarehouse =
                repository.findAllFiltered(
                        null,
                        warehouse1.getId(),
                        pageable
                );

        var combined =
                repository.findAllFiltered(
                        product1.getId(),
                        warehouse1.getId(),
                        pageable
                );

        assertThat(all.getContent())
                .hasSize(3);

        assertThat(byProduct.getContent())
                .hasSize(2)
                .allMatch(stock ->
                        stock.getProduct()
                                .getId()
                                .equals(product1.getId())
                );

        assertThat(byWarehouse.getContent())
                .hasSize(2)
                .allMatch(stock ->
                        stock.getWarehouse()
                                .getId()
                                .equals(warehouse1.getId())
                );

        assertThat(combined.getContent())
                .hasSize(1);

        assertThat(
                combined.getContent()
                        .getFirst()
                        .getProduct()
                        .getId()
        ).isEqualTo(product1.getId());

        assertThat(
                combined.getContent()
                        .getFirst()
                        .getWarehouse()
                        .getId()
        ).isEqualTo(warehouse1.getId());
    }

    @Test
    void shouldFindLowStock() {
        Category category =
                createCategory();

        Product product1 =
                createProduct("SKU-1", category);

        Product product2 =
                createProduct("SKU-2", category);

        Product product3 =
                createProduct("SKU-3", category);

        Warehouse warehouse =
                createWarehouse("WH-001");

        repository.saveAllAndFlush(
                List.of(
                        new Stock(
                                product1,
                                warehouse,
                                5,
                                5
                        ),
                        new Stock(
                                product2,
                                warehouse,
                                3,
                                10
                        ),
                        new Stock(
                                product3,
                                warehouse,
                                20,
                                5
                        )
                )
        );

        Pageable pageable =
                PageRequest.of(0, 20);

        var result =
                repository.findLowStock(
                        null,
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(stock ->
                        stock.getProduct().getSku()
                )
                .containsExactlyInAnyOrder(
                        "SKU-1",
                        "SKU-2"
                );
    }

    @Test
    void shouldFilterLowStockByWarehouse() {
        Category category =
                createCategory();

        Product product1 =
                createProduct("SKU-1", category);

        Product product2 =
                createProduct("SKU-2", category);

        Warehouse warehouse1 =
                createWarehouse("WH-001");

        Warehouse warehouse2 =
                createWarehouse("WH-002");

        repository.saveAllAndFlush(
                List.of(
                        new Stock(
                                product1,
                                warehouse1,
                                2,
                                5
                        ),
                        new Stock(
                                product2,
                                warehouse2,
                                1,
                                5
                        )
                )
        );

        Pageable pageable =
                PageRequest.of(0, 20);

        var result =
                repository.findLowStock(
                        warehouse1.getId(),
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(
                result.getContent()
                        .getFirst()
                        .getWarehouse()
                        .getId()
        ).isEqualTo(warehouse1.getId());
    }

    @Test
    void shouldRejectNegativeQuantityAtDatabaseLevel() {
        Category category =
                createCategory();

        Product product =
                createProduct("SKU-1", category);

        Warehouse warehouse =
                createWarehouse("WH-001");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO stocks
                            (product_id,
                             warehouse_id,
                             quantity,
                             minimum_stock)
                        VALUES (?, ?, ?, ?)
                        """,
                        product.getId(),
                        warehouse.getId(),
                        -1,
                        0
                )
        );
    }

    @Test
    void shouldRejectNegativeMinimumStockAtDatabaseLevel() {
        Category category =
                createCategory();

        Product product =
                createProduct("SKU-1", category);

        Warehouse warehouse =
                createWarehouse("WH-001");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO stocks
                            (product_id,
                             warehouse_id,
                             quantity,
                             minimum_stock)
                        VALUES (?, ?, ?, ?)
                        """,
                        product.getId(),
                        warehouse.getId(),
                        10,
                        -1
                )
        );
    }

    @Test
    void shouldRejectNonExistingProductForeignKey() {
        Warehouse warehouse =
                createWarehouse("WH-001");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO stocks
                            (product_id,
                             warehouse_id,
                             quantity,
                             minimum_stock)
                        VALUES (?, ?, ?, ?)
                        """,
                        999999L,
                        warehouse.getId(),
                        10,
                        0
                )
        );
    }

    @Test
    void shouldRejectNonExistingWarehouseForeignKey() {
        Category category =
                createCategory();

        Product product =
                createProduct("SKU-1", category);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO stocks
                            (product_id,
                             warehouse_id,
                             quantity,
                             minimum_stock)
                        VALUES (?, ?, ?, ?)
                        """,
                        product.getId(),
                        999999L,
                        10,
                        0
                )
        );
    }


    @Test
    void shouldAcquirePessimisticWriteLockWhenFindingStockForUpdate() {
        Category category = createCategory();

        Product product = createProduct("SKU-001", category);

        Warehouse warehouse = createWarehouse("WH-001");

        Stock stock = repository.saveAndFlush(new Stock(product, warehouse, 10, 5));

        entityManager.clear();

        Stock lockedStock = repository.findByProductIdAndWarehouseIdForUpdate(product.getId(), warehouse.getId())
                .orElseThrow();

        assertThat(lockedStock.getId()).isEqualTo(stock.getId());
        assertThat(entityManager.getLockMode(lockedStock)).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }
}