package com.burgosfacundo.inventory.inventory_movement.service;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.category.repository.CategoryRepository;
import com.burgosfacundo.inventory.config.IntegrationTest;
import com.burgosfacundo.inventory.inventory_movement.dto.InventoryMovementRequest;
import com.burgosfacundo.inventory.inventory_movement.model.InventoryMovement;
import com.burgosfacundo.inventory.inventory_movement.model.MovementType;
import com.burgosfacundo.inventory.inventory_movement.repository.InventoryMovementRepository;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product.repository.ProductRepository;
import com.burgosfacundo.inventory.stock.model.Stock;
import com.burgosfacundo.inventory.stock.repository.StockRepository;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import com.burgosfacundo.inventory.warehouse.repository.WarehouseRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class InventoryMovementTransactionIT extends IntegrationTest {

    @Autowired
    private InventoryMovementService service;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private EntityManager entityManager;

    /*
     * Reemplazamos solamente este repository.
     * El resto sigue utilizando MySQL real.
     */
    @MockitoBean
    private InventoryMovementRepository movementRepository;

    @AfterEach
    void cleanUp() {
        stockRepository.deleteAll();
        warehouseRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldRollbackStockUpdateWhenMovementPersistenceFails() {
        Category category =
                categoryRepository.saveAndFlush(
                        new Category(
                                "Category",
                                null
                        )
                );

        Product product =
                productRepository.saveAndFlush(
                        new Product(
                                "SKU-001",
                                "Product",
                                null,
                                new BigDecimal("100.00"),
                                category
                        )
                );

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

        Warehouse warehouse =
                warehouseRepository.saveAndFlush(
                        new Warehouse(
                                "WH-001",
                                "Main Warehouse",
                                address
                        )
                );

        Stock stock =
                stockRepository.saveAndFlush(
                        new Stock(
                                product,
                                warehouse,
                                10,
                                5
                        )
                );

        Long stockId = stock.getId();

        InventoryMovementRequest request =
                new InventoryMovementRequest(
                        product.getId(),
                        warehouse.getId(),
                        MovementType.OUT,
                        4
                );

        when(
                movementRepository.save(
                        any(InventoryMovement.class)
                )
        ).thenAnswer(invocation -> {

            /*
             * Forzamos a Hibernate a ejecutar el UPDATE
             * de Stock contra MySQL antes del fallo.
             */
            entityManager.flush();

            throw new DataIntegrityViolationException(
                    "Simulated movement persistence failure"
            );
        });

        assertThrows(
                DataIntegrityViolationException.class,
                () -> service.create(request)
        );

        Stock persistedStock =
                stockRepository.findById(stockId)
                        .orElseThrow();

        assertThat(persistedStock.getQuantity())
                .isEqualTo(10);
    }
}