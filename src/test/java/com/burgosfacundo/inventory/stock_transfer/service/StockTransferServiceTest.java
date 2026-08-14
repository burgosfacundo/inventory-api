package com.burgosfacundo.inventory.stock_transfer.service;

import com.burgosfacundo.inventory.category.model.Category;
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
import com.burgosfacundo.inventory.stock_transfer.dto.StockTransferRequest;
import com.burgosfacundo.inventory.stock_transfer.exception.InvalidTransferDateRangeException;
import com.burgosfacundo.inventory.stock_transfer.exception.SameWarehouseTransferException;
import com.burgosfacundo.inventory.stock_transfer.exception.StockTransferNotFoundException;
import com.burgosfacundo.inventory.stock_transfer.model.StockTransfer;
import com.burgosfacundo.inventory.stock_transfer.repository.StockTransferRepository;
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
import org.mockito.InOrder;
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
class StockTransferServiceTest {

    static Stream<Arguments> transferFilters() {
        LocalDateTime from = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 8, 31, 23, 59);

        return Stream.of(
                Arguments.of(null, null, null, null, null),
                Arguments.of(1L, null, null, null, null),
                Arguments.of(null, 10L, null, null, null),
                Arguments.of(null, null, 20L, null, null),
                Arguments.of(1L, 10L, 20L, null, null),
                Arguments.of(1L, 10L, 20L, from, to)
        );
    }

    @Mock
    private StockTransferRepository repository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private StockTransferServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StockTransferServiceImpl(repository, stockRepository, movementRepository, productRepository, warehouseRepository);
    }

    private Product product() {
        Category category = new Category("Category", null);

        Product product = new Product("SKU-001", "Product", null,
                new BigDecimal("100.00"), category);

        ReflectionTestUtils.setField(product, "id", 1L);

        return product;
    }

    private Warehouse warehouse(Long id, String code) {
        Address address = new Address(
                        "Avenida Independencia",
                        "1234",
                        "B7600",
                        "Mar del Plata",
                        "Buenos Aires",
                        "AR",
                        new BigDecimal("-38.0055000"),
                        new BigDecimal("-57.5426000")
                );

        Warehouse warehouse = new Warehouse(code, "Warehouse " + code, address);

        ReflectionTestUtils.setField(warehouse, "id", id);

        return warehouse;
    }

    private Stock stock(Long id, Product product, Warehouse warehouse, int quantity, int minimumStock) {
        Stock stock = new Stock(product, warehouse, quantity, minimumStock);

        ReflectionTestUtils.setField(stock, "id", id);

        return stock;
    }

    //Save

    @Test
    void shouldTransferStockBetweenExistingWarehouses() {
        Product product = product();

        Warehouse source = warehouse(10L, "WH-001");

        Warehouse destination = warehouse(20L, "WH-002");

        Stock sourceStock = stock(100L, product, source, 20, 5);

        Stock destinationStock = stock(200L, product, destination, 5, 2);

        StockTransferRequest request = new StockTransferRequest(1L, 10L,
                20L, 8);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(warehouseRepository.findById(10L))
                .thenReturn(Optional.of(source));

        when(warehouseRepository.findById(20L))
                .thenReturn(Optional.of(destination));

        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 10L))
                .thenReturn(Optional.of(sourceStock));

        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 20L))
                .thenReturn(Optional.of(destinationStock));

        when(repository.save(any(StockTransfer.class)))
                .thenAnswer(invocation -> {
                    StockTransfer transfer = invocation.getArgument(0);

                    ReflectionTestUtils.setField(transfer, "id", 300L);
                    return transfer;
                });

        var response = service.create(request);

        assertThat(sourceStock.getQuantity()).isEqualTo(12);
        assertThat(destinationStock.getQuantity()).isEqualTo(13);
        assertThat(sourceStock.getMinimumStock()).isEqualTo(5);
        assertThat(destinationStock.getMinimumStock()).isEqualTo(2);
        assertThat(response.id()).isEqualTo(300L);
        assertThat(response.product().id()).isEqualTo(1L);
        assertThat(response.sourceWarehouse().id()).isEqualTo(10L);
        assertThat(response.destinationWarehouse().id()).isEqualTo(20L);
        assertThat(response.quantity()).isEqualTo(8);

        verify(stockRepository, never()).save(any(Stock.class));
        verify(repository).save(any(StockTransfer.class));


        //Verify save Inventory Movement
        ArgumentCaptor<InventoryMovement> movementCaptor = ArgumentCaptor.forClass(InventoryMovement.class);

        verify(movementRepository, times(2)).save(movementCaptor.capture());

        List<InventoryMovement> movements = movementCaptor.getAllValues();

        InventoryMovement outMovement = movements.get(0);

        InventoryMovement inMovement = movements.get(1);

        assertThat(outMovement.getType()).isEqualTo(MovementType.OUT);
        assertThat(outMovement.getProduct()).isSameAs(product);
        assertThat(outMovement.getWarehouse()).isSameAs(source);
        assertThat(outMovement.getQuantity()).isEqualTo(8);
        assertThat(inMovement.getType()).isEqualTo(MovementType.IN);
        assertThat(inMovement.getProduct()).isSameAs(product);
        assertThat(inMovement.getWarehouse()).isSameAs(destination);
        assertThat(inMovement.getQuantity()).isEqualTo(8);
    }


    @Test
    void shouldLockStocksInWarehouseIdOrder() {
        Product product = product();

        Warehouse source = warehouse(8L, "WH-008");

        Warehouse destination = warehouse(3L, "WH-003");

        Stock sourceStock = stock(100L, product, source, 20, 5);

        Stock destinationStock = stock(200L, product, destination, 5, 2);

        StockTransferRequest request = new StockTransferRequest(1L, 8L, 3L, 5);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(warehouseRepository.findById(8L))
                .thenReturn(Optional.of(source));

        when(warehouseRepository.findById(3L))
                .thenReturn(Optional.of(destination));


        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 3L))
                .thenReturn(Optional.of(destinationStock));

        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 8L))
                .thenReturn(Optional.of(sourceStock));

        when(repository.save(any(StockTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(request);

        InOrder inOrder = inOrder(stockRepository);

        inOrder.verify(stockRepository).findByProductIdAndWarehouseIdForUpdate(1L, 3L);
        inOrder.verify(stockRepository).findByProductIdAndWarehouseIdForUpdate(1L, 8L);

        assertThat(sourceStock.getQuantity()).isEqualTo(15);
        assertThat(destinationStock.getQuantity()).isEqualTo(10);
    }


    @Test
    void shouldCreateDestinationStockWhenItDoesNotExist() {
        Product product = product();

        Warehouse source = warehouse(10L, "WH-001");

        Warehouse destination = warehouse(20L, "WH-002");

        Stock sourceStock = stock(100L, product, source, 20, 5);

        StockTransferRequest request = new StockTransferRequest(1L, 10L, 20L, 8);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(warehouseRepository.findById(10L))
                .thenReturn(Optional.of(source));

        when(warehouseRepository.findById(20L))
                .thenReturn(Optional.of(destination));

        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 10L))
                .thenReturn(Optional.of(sourceStock));

        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 20L))
                .thenReturn(Optional.empty());

        when(repository.save(any(StockTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(request);

        assertThat(sourceStock.getQuantity()).isEqualTo(12);

        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);

        verify(stockRepository).save(stockCaptor.capture());

        Stock createdDestination = stockCaptor.getValue();

        assertThat(createdDestination.getProduct()).isSameAs(product);
        assertThat(createdDestination.getWarehouse()).isSameAs(destination);
        assertThat(createdDestination.getQuantity()).isEqualTo(8);
        assertThat(createdDestination.getMinimumStock()).isZero();

        verify(movementRepository, times(2)).save(any(InventoryMovement.class));
        verify(repository).save(any(StockTransfer.class));
    }


    @Test
    void shouldThrowWhenProductDoesNotExist() {
        StockTransferRequest request = new StockTransferRequest(99L, 10L, 20L, 5);

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.create(request));

        verify(warehouseRepository, never()).findById(anyLong());
        verify(stockRepository, never()).findByProductIdAndWarehouseIdForUpdate(anyLong(), anyLong());
        verify(movementRepository, never()).save(any(InventoryMovement.class));
        verify(repository, never()).save(any(StockTransfer.class));
    }

    @Test
    void shouldThrowWhenSourceWarehouseDoesNotExist() {
        Product product = product();
        StockTransferRequest request = new StockTransferRequest(1L, 99L, 20L, 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> service.create(request));

        verify(warehouseRepository, never()).findById(20L);
        verify(stockRepository, never()).findByProductIdAndWarehouseIdForUpdate(anyLong(), anyLong());
        verify(movementRepository, never()).save(any(InventoryMovement.class));
        verify(repository, never()).save(any(StockTransfer.class));
    }

    @Test
    void shouldThrowWhenDestinationWarehouseDoesNotExist() {
        Product product = product();
        Warehouse source = warehouse(10L, "WH-001");
        StockTransferRequest request = new StockTransferRequest(1L, 10L, 99L, 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(source));
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> service.create(request));

        verify(stockRepository, never()).findByProductIdAndWarehouseIdForUpdate(anyLong(), anyLong());
        verify(movementRepository, never()).save(any(InventoryMovement.class));
        verify(repository, never()).save(any(StockTransfer.class));
    }

    @Test
    void shouldRejectTransferToSameWarehouse() {
        Product product = product();
        Warehouse warehouse = warehouse(10L, "WH-001");
        StockTransferRequest request = new StockTransferRequest(1L, 10L, 10L, 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(warehouse));

        assertThrows(SameWarehouseTransferException.class, () -> service.create(request));

        verify(stockRepository, never()).findByProductIdAndWarehouseIdForUpdate(anyLong(), anyLong());
        verify(movementRepository, never()).save(any(InventoryMovement.class));
        verify(repository, never()).save(any(StockTransfer.class));
    }

    @Test
    void shouldThrowWhenSourceStockDoesNotExist() {
        Product product = product();
        Warehouse source = warehouse(10L, "WH-001");
        Warehouse destination = warehouse(20L, "WH-002");
        Stock destinationStock = stock(200L, product, destination, 5, 2);

        StockTransferRequest request = new StockTransferRequest(1L, 10L, 20L, 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(source));
        when(warehouseRepository.findById(20L)).thenReturn(Optional.of(destination));
        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 10L)).thenReturn(Optional.empty());
        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 20L)).thenReturn(Optional.of(destinationStock));

        assertThrows(StockNotFoundException.class, () -> service.create(request));

        assertThat(destinationStock.getQuantity()).isEqualTo(5);

        verify(stockRepository, never()).save(any(Stock.class));
        verify(movementRepository, never()).save(any(InventoryMovement.class));
        verify(repository, never()).save(any(StockTransfer.class));
    }

    @Test
    void shouldRejectTransferWhenSourceStockIsInsufficient() {
        Product product = product();
        Warehouse source = warehouse(10L, "WH-001");
        Warehouse destination = warehouse(20L, "WH-002");

        Stock sourceStock = stock(100L, product, source, 5, 2);
        Stock destinationStock = stock(200L, product, destination, 10, 2);

        StockTransferRequest request = new StockTransferRequest(1L, 10L, 20L, 8);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(source));
        when(warehouseRepository.findById(20L)).thenReturn(Optional.of(destination));
        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 10L)).thenReturn(Optional.of(sourceStock));
        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 20L)).thenReturn(Optional.of(destinationStock));

        assertThrows(InsufficientStockException.class, () -> service.create(request));

        assertThat(sourceStock.getQuantity()).isEqualTo(5);
        assertThat(destinationStock.getQuantity()).isEqualTo(10);

        verify(stockRepository, never()).save(any(Stock.class));
        verify(movementRepository, never()).save(any(InventoryMovement.class));
        verify(repository, never()).save(any(StockTransfer.class));
    }

    @Test
    void shouldAllowTransferResultingInZeroSourceStock() {
        Product product = product();
        Warehouse source = warehouse(10L, "WH-001");
        Warehouse destination = warehouse(20L, "WH-002");

        Stock sourceStock = stock(100L, product, source, 8, 5);
        Stock destinationStock = stock(200L, product, destination, 4, 2);

        StockTransferRequest request = new StockTransferRequest(1L, 10L, 20L, 8);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(source));
        when(warehouseRepository.findById(20L)).thenReturn(Optional.of(destination));
        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 10L)).thenReturn(Optional.of(sourceStock));
        when(stockRepository.findByProductIdAndWarehouseIdForUpdate(1L, 20L)).thenReturn(Optional.of(destinationStock));
        when(repository.save(any(StockTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(request);

        assertThat(sourceStock.getQuantity()).isZero();
        assertThat(destinationStock.getQuantity()).isEqualTo(12);
        assertThat(sourceStock.isLowStock()).isTrue();

        verify(movementRepository, times(2)).save(any(InventoryMovement.class));
        verify(repository).save(any(StockTransfer.class));
    }


    //Find By Id

    @Test
    void shouldFindStockTransferById() {
        Product product = product();
        Warehouse source = warehouse(10L, "WH-001");
        Warehouse destination = warehouse(20L, "WH-002");

        StockTransfer transfer = new StockTransfer(product, source, destination, 8);
        ReflectionTestUtils.setField(transfer, "id", 300L);

        when(repository.findWithRelationsById(300L)).thenReturn(Optional.of(transfer));

        var response = service.findById(300L);

        assertThat(response.id()).isEqualTo(300L);
        assertThat(response.product().id()).isEqualTo(1L);
        assertThat(response.product().sku()).isEqualTo("SKU-001");
        assertThat(response.sourceWarehouse().id()).isEqualTo(10L);
        assertThat(response.sourceWarehouse().code()).isEqualTo("WH-001");
        assertThat(response.destinationWarehouse().id()).isEqualTo(20L);
        assertThat(response.destinationWarehouse().code()).isEqualTo("WH-002");
        assertThat(response.quantity()).isEqualTo(8);
        assertThat(response.createdAt()).isNotNull();

        verify(repository).findWithRelationsById(300L);
    }

    @Test
    void shouldThrowWhenStockTransferDoesNotExist() {
        when(repository.findWithRelationsById(999L)).thenReturn(Optional.empty());

        assertThrows(StockTransferNotFoundException.class, () -> service.findById(999L));

        verify(repository).findWithRelationsById(999L);
    }

    //Find All
    @ParameterizedTest
    @MethodSource("transferFilters")
    void shouldFindStockTransfersWithFilters(Long productId, Long sourceWarehouseId, Long destinationWarehouseId,
            LocalDateTime from, LocalDateTime to) {
        Product product = product();
        Warehouse source = warehouse(10L, "WH-001");
        Warehouse destination = warehouse(20L, "WH-002");

        StockTransfer transfer = new StockTransfer(product, source, destination, 8);
        ReflectionTestUtils.setField(transfer, "id", 300L);

        Pageable pageable = PageRequest.of(0, 20);

        when(repository.findAllFiltered(productId, sourceWarehouseId, destinationWarehouseId, from, to, pageable))
                .thenReturn(new PageImpl<>(List.of(transfer), pageable, 1));

        var result = service.findAll(productId, sourceWarehouseId, destinationWarehouseId, from, to, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().id()).isEqualTo(300L);
        assertThat(result.getContent().getFirst().quantity()).isEqualTo(8);

        verify(repository).findAllFiltered(productId, sourceWarehouseId, destinationWarehouseId, from, to, pageable);
    }


    @Test
    void shouldReturnEmptyPageWhenNoStockTransferMatchesFilters() {
        Pageable pageable = PageRequest.of(0, 20);

        when(repository.findAllFiltered(99L, null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var result = service.findAll(99L, null, null, null, null, pageable);

        assertThat(result).isEmpty();

        verify(repository).findAllFiltered(99L, null, null, null, null, pageable);
    }


    //Validate Range

    @Test
    void shouldRejectInvalidDateRange() {
        LocalDateTime from = LocalDateTime.of(2026, 8, 20, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 8, 10, 0, 0);

        Pageable pageable = PageRequest.of(0, 20);

        assertThrows(InvalidTransferDateRangeException.class,
                () -> service.findAll(null, null, null, from, to, pageable));

        verify(repository, never()).findAllFiltered(any(), any(), any(), any(), any(), any(Pageable.class));
    }


    @Test
    void shouldAllowEqualFromAndToDates() {
        LocalDateTime date = LocalDateTime.of(2026, 8, 20, 12, 0);
        Pageable pageable = PageRequest.of(0, 20);

        when(repository.findAllFiltered(null, null, null, date, date, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var result = service.findAll(null, null, null, date, date, pageable);

        assertThat(result).isEmpty();

        verify(repository).findAllFiltered(null, null, null, date, date, pageable);
    }

}