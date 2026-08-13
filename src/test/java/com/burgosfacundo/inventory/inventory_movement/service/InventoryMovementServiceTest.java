package com.burgosfacundo.inventory.inventory_movement.service;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.inventory_movement.dto.InventoryMovementRequest;
import com.burgosfacundo.inventory.inventory_movement.exception.InvalidMovementDateRangeException;
import com.burgosfacundo.inventory.inventory_movement.exception.InventoryMovementNotFoundException;
import com.burgosfacundo.inventory.inventory_movement.exception.MovementQuantityInvalidException;
import com.burgosfacundo.inventory.inventory_movement.model.InventoryMovement;
import com.burgosfacundo.inventory.inventory_movement.model.MovementType;
import com.burgosfacundo.inventory.inventory_movement.repository.InventoryMovementRepository;
import com.burgosfacundo.inventory.product.exception.ProductNotFoundException;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product.repository.ProductRepository;
import com.burgosfacundo.inventory.stock.exception.InsufficientStockException;
import com.burgosfacundo.inventory.stock.exception.StockNotFoundException;
import com.burgosfacundo.inventory.stock.model.Stock;
import com.burgosfacundo.inventory.stock.repository.StockRepository;
import com.burgosfacundo.inventory.warehouse.exception.WarehouseNotFoundException;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import com.burgosfacundo.inventory.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceTest {

    @Mock
    private InventoryMovementRepository repository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private InventoryMovementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InventoryMovementServiceImpl(repository, stockRepository, productRepository, warehouseRepository);
    }

    private Product product() {
        Category category = new Category("Category", null);

        Product product = new Product("SKU-001", "Product",
                null, new BigDecimal("100.00"), category);

        ReflectionTestUtils.setField(product, "id", 1L);

        return product;
    }

    private Warehouse warehouse() {
        Address address = new Address("Avenida Independencia", "1234", "B7600",
                "Mar del Plata", "Buenos Aires", "AR",
                new BigDecimal("-38.0055000"), new BigDecimal("-57.5426000"));

        Warehouse warehouse = new Warehouse("WH-001", "Main Warehouse", address);

        ReflectionTestUtils.setField(warehouse, "id", 2L);

        return warehouse;
    }

    private Stock stock(Product product, Warehouse warehouse) {
        Stock stock = new Stock(product, warehouse, 10, 5);

        ReflectionTestUtils.setField(stock, "id", 1L);

        return stock;
    }

    private InventoryMovement movement(Product product, Warehouse warehouse) {
        InventoryMovement movement = new InventoryMovement(product, warehouse, MovementType.IN, 10);

        ReflectionTestUtils.setField(movement, "id", 100L);

        return movement;
    }

    //Save In
    @Test
    void shouldCreateInMovementWithExistingStock() {
        Product product = product();
        Warehouse warehouse = warehouse();

        Stock stock = stock(product, warehouse);

        InventoryMovementRequest request = new InventoryMovementRequest(1L, 2L,
                MovementType.IN, 5);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(warehouseRepository.findById(2L))
                .thenReturn(Optional.of(warehouse));

        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 2L))
                .thenReturn(Optional.of(stock));

        when(repository.save(any(InventoryMovement.class)))
                .thenAnswer(invocation -> {
                    InventoryMovement movement = invocation.getArgument(0);

                    ReflectionTestUtils.setField(movement, "id", 100L);

                    return movement;
                });

        var response = service.create(request);

        assertThat(stock.getQuantity()).isEqualTo(15);
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.type()).isEqualTo(MovementType.IN);
        assertThat(response.quantity()).isEqualTo(5);
        assertThat(response.product().id()).isEqualTo(1L);
        assertThat(response.warehouse().id()).isEqualTo(2L);

        verify(stockRepository).findByProductIdAndWarehouseIdForUpdate(1L, 2L);
        verify(stockRepository, never()).save(any(Stock.class));
        verify(repository).save(any(InventoryMovement.class));
    }

    @Test
    void shouldCreateStockOnFirstInMovement() {
        Product product = product();
        Warehouse warehouse = warehouse();

        InventoryMovementRequest request = new InventoryMovementRequest(1L, 2L,
                MovementType.IN, 20);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));

        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 2L))
                .thenReturn(Optional.empty());

        when(repository.save(any(InventoryMovement.class)))
                .thenAnswer(invocation -> {
                    InventoryMovement movement = invocation.getArgument(0);

                    ReflectionTestUtils.setField(movement, "id", 100L);

                    return movement;
                });

        service.create(request);

        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);

        verify(stockRepository).save(stockCaptor.capture());

        Stock createdStock = stockCaptor.getValue();

        assertThat(createdStock.getProduct()).isSameAs(product);
        assertThat(createdStock.getWarehouse()).isSameAs(warehouse);
        assertThat(createdStock.getQuantity()).isEqualTo(20);
        assertThat(createdStock.getMinimumStock()).isZero();
        assertThat(createdStock.isLowStock()).isFalse();
        verify(repository).save(any(InventoryMovement.class));
    }

    //Save  Out

    @Test
    void shouldCreateOutMovementWithSufficientStock() {
        Product product = product();
        Warehouse warehouse = warehouse();

        Stock stock = stock(product, warehouse);

        InventoryMovementRequest request = new InventoryMovementRequest(1L, 2L,
                MovementType.OUT, 4);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));

        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 2L))
                .thenReturn(Optional.of(stock));

        when(repository.save(any(InventoryMovement.class)))
                .thenAnswer(invocation -> {
                    InventoryMovement movement = invocation.getArgument(0);

                    ReflectionTestUtils.setField(movement, "id", 100L);

                    return movement;
                });

        var response = service.create(request);

        assertThat(stock.getQuantity()).isEqualTo(6);
        assertThat(response.type()).isEqualTo(MovementType.OUT);
        assertThat(response.quantity()).isEqualTo(4);
        verify(stockRepository, never()).save(any(Stock.class));
        verify(repository).save(any(InventoryMovement.class));
    }

    @Test
    void shouldAllowOutMovementResultingInZeroStock() {
        Product product = product();
        Warehouse warehouse = warehouse();

        Stock stock = stock(product, warehouse);

        InventoryMovementRequest request = new InventoryMovementRequest(1L, 2L,
                MovementType.OUT, 10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));

        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 2L))
                .thenReturn(Optional.of(stock));

        when(repository.save(any(InventoryMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(request);

        assertThat(stock.getQuantity()).isZero();
        assertThat(stock.isLowStock()).isTrue();

        verify(repository).save(any(InventoryMovement.class));
    }

    @Test
    void shouldRejectOutMovementWhenStockDoesNotExist() {
        Product product = product();
        Warehouse warehouse = warehouse();

        InventoryMovementRequest request = new InventoryMovementRequest(1L, 2L,
                MovementType.OUT, 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));

        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 2L))
                .thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class,
                () -> service.create(request));

        verify(stockRepository, never()).save(any(Stock.class));
        verify(repository, never()).save(any(InventoryMovement.class));
    }

    @Test
    void shouldRejectOutMovementWhenStockIsInsufficient() {
        Product product = product();
        Warehouse warehouse = warehouse();

        Stock stock = stock(product, warehouse);

        InventoryMovementRequest request = new InventoryMovementRequest(1L, 2L,
                MovementType.OUT, 11);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));

        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 2L))
                .thenReturn(Optional.of(stock));

        assertThrows(InsufficientStockException.class,
                () -> service.create(request));

        assertThat(stock.getQuantity()).isEqualTo(10);

        verify(repository, never()).save(any(InventoryMovement.class));

        verify(stockRepository, never()).save(any(Stock.class));
    }

    //save required validation

    @Test
    void shouldRejectMovementWhenProductDoesNotExist() {
        InventoryMovementRequest request =
                new InventoryMovementRequest(
                        99L,
                        2L,
                        MovementType.IN,
                        10
                );

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> service.create(request)
        );

        verify(
                warehouseRepository,
                never()
        ).findById(anyLong());

        verify(
                stockRepository,
                never()
        ).findByProductIdAndWarehouseIdForUpdate(
                anyLong(),
                anyLong()
        );

        verify(
                repository,
                never()
        ).save(any(InventoryMovement.class));
    }

    @Test
    void shouldRejectMovementWhenWarehouseDoesNotExist() {
        Product product = product();

        InventoryMovementRequest request =
                new InventoryMovementRequest(
                        1L,
                        99L,
                        MovementType.IN,
                        10
                );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(warehouseRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                WarehouseNotFoundException.class,
                () -> service.create(request)
        );

        verify(
                stockRepository,
                never()
        ).findByProductIdAndWarehouseIdForUpdate(
                anyLong(),
                anyLong()
        );

        verify(
                repository,
                never()
        ).save(any(InventoryMovement.class));
    }

    @Test
    void shouldRejectInvalidMovementQuantityBeforeModifyingStock() {
        Product product = product();
        Warehouse warehouse = warehouse();

        InventoryMovementRequest request =
                new InventoryMovementRequest(
                        1L,
                        2L,
                        MovementType.IN,
                        0
                );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(warehouseRepository.findById(2L))
                .thenReturn(Optional.of(warehouse));

        assertThrows(
                MovementQuantityInvalidException.class,
                () -> service.create(request)
        );

        /*
         * InventoryMovement is constructed before Stock is touched.
         */
        verify(
                stockRepository,
                never()
        ).findByProductIdAndWarehouseIdForUpdate(
                anyLong(),
                anyLong()
        );

        verify(
                stockRepository,
                never()
        ).save(any(Stock.class));

        verify(
                repository,
                never()
        ).save(any(InventoryMovement.class));
    }

    //Find By Id

    @Test
    void shouldFindMovementById() {
        Product product = product();
        Warehouse warehouse = warehouse();

        InventoryMovement movement =
                movement(
                        product,
                        warehouse
                );

        when(repository.findWithRelationsById(100L)).thenReturn(Optional.of(movement));

        var response =
                service.findById(100L);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.product().id()).isEqualTo(1L);
        assertThat(response.product().sku()).isEqualTo("SKU-001");
        assertThat(response.warehouse().id()).isEqualTo(2L);
        assertThat(response.warehouse().code()).isEqualTo("WH-001");
        assertThat(response.type()).isEqualTo(MovementType.IN);
        assertThat(response.quantity()).isEqualTo(10);
        assertThat(response.createdAt()).isNotNull();

        verify(repository).findWithRelationsById(100L);
    }

    @Test
    void shouldThrowWhenMovementDoesNotExist() {
        when(
                repository
                        .findWithRelationsById(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                InventoryMovementNotFoundException.class,
                () -> service.findById(99L)
        );

        verify(repository)
                .findWithRelationsById(99L);
    }

    //Find All

    static Stream<Arguments> movementFilters() {
        LocalDateTime from =
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        0,
                        0
                );

        LocalDateTime to =
                LocalDateTime.of(
                        2026,
                        8,
                        31,
                        23,
                        59
                );

        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                Arguments.of(
                        1L,
                        null,
                        null,
                        null,
                        null
                ),
                Arguments.of(
                        null,
                        2L,
                        null,
                        null,
                        null
                ),
                Arguments.of(
                        null,
                        null,
                        MovementType.IN,
                        null,
                        null
                ),
                Arguments.of(
                        1L,
                        2L,
                        MovementType.OUT,
                        from,
                        to
                )
        );
    }

    @ParameterizedTest
    @MethodSource("movementFilters")
    void shouldFindMovementsWithFilters(
            Long productId,
            Long warehouseId,
            MovementType type,
            LocalDateTime from,
            LocalDateTime to
    ) {
        Product product = product();
        Warehouse warehouse = warehouse();

        InventoryMovement movement =
                movement(
                        product,
                        warehouse
                );

        Pageable pageable =
                PageRequest.of(
                        0,
                        20
                );

        when(
                repository.findAllFiltered(
                        productId,
                        warehouseId,
                        type,
                        from,
                        to,
                        pageable
                )
        ).thenReturn(
                new PageImpl<>(
                        List.of(movement),
                        pageable,
                        1
                )
        );

        var result =
                service.findAll(
                        productId,
                        warehouseId,
                        type,
                        from,
                        to,
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(
                result.getContent()
                        .getFirst()
                        .id()
        ).isEqualTo(100L);

        verify(repository)
                .findAllFiltered(
                        productId,
                        warehouseId,
                        type,
                        from,
                        to,
                        pageable
                );
    }

    @Test
    void shouldReturnEmptyPageWhenNoMovementMatchesFilters() {
        Pageable pageable =
                PageRequest.of(
                        0,
                        20
                );

        when(repository.findAllFiltered(99L, null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var result = service.findAll(99L, null, null, null, null, pageable);

        assertThat(result).isEmpty();

        verify(repository).findAllFiltered(99L, null, null, null, null, pageable);
    }

    @Test
    void shouldRejectInvalidDateRange() {
        LocalDateTime from = LocalDateTime.of(2026, 8, 20, 0, 0);

        LocalDateTime to = LocalDateTime.of(2026, 8, 10, 0, 0);

        assertThrows(InvalidMovementDateRangeException.class,
                () -> service.findAll(null, null, null,from,to,
                        PageRequest.of(0, 20)));

        verify(repository, never()).findAllFiltered(any(), any(), any(), any(), any(), any(Pageable.class));
    }
}