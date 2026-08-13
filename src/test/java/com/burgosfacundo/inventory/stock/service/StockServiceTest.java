package com.burgosfacundo.inventory.stock.service;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.stock.dto.StockMinimumRequest;
import com.burgosfacundo.inventory.stock.exception.StockNotFoundException;
import com.burgosfacundo.inventory.stock.model.Stock;
import com.burgosfacundo.inventory.stock.repository.StockRepository;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {
    @Mock
    private StockRepository repository;

    private StockService service;

    @BeforeEach
    void setUp() {
        service = new StockServiceImpl(repository);
    }

    private Product product() {
        Category category =
                new Category(
                        "Category",
                        null
                );

        Product product =
                new Product(
                        "SKU-001",
                        "Product",
                        null,
                        new BigDecimal("100.00"),
                        category
                );

        ReflectionTestUtils.setField(
                product,
                "id",
                1L
        );

        return product;
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

        Warehouse warehouse =
                new Warehouse(
                        "WH-001",
                        "Main Warehouse",
                        address
                );

        ReflectionTestUtils.setField(
                warehouse,
                "id",
                1L
        );

        return warehouse;
    }


    private Stock createStock(int quantity) {
        Stock stock =
                new Stock(
                        product(),
                        warehouse(),
                        quantity,
                        5
                );

        ReflectionTestUtils.setField(
                stock,
                "id",
                1L
        );

        return stock;
    }

    static Stream<Arguments> findAllFilters() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(1L, null),
                Arguments.of(null, 2L),
                Arguments.of(1L, 2L)
        );
    }

    static Stream<Arguments> lowStockWarehouseFilters() {
        return Stream.of(
                Arguments.of((Long) null),
                Arguments.of(1L)
        );
    }

    //Find by Id
    @Test
    void shouldFindStockById() {
        Stock stock = createStock(10);

        when(repository.findWithRelationsById(1L))
                .thenReturn(Optional.of(stock));

        var response =
                service.findById(1L);

        assertThat(response.id()).isEqualTo(stock.getId());
        assertThat(response.quantity()).isEqualTo(stock.getQuantity());
        assertThat(response.minimumStock()).isEqualTo(stock.getMinimumStock());
        assertThat(response.warehouse().id()).isEqualTo(stock.getWarehouse().getId());
        assertThat(response.warehouse().name()).isEqualTo(stock.getWarehouse().getName());
        assertThat(response.warehouse().code()).isEqualTo(stock.getWarehouse().getCode());
        assertThat(response.product().id()).isEqualTo(stock.getProduct().getId());
        assertThat(response.product().sku()).isEqualTo(stock.getProduct().getSku());
        assertThat(response.product().name()).isEqualTo(stock.getProduct().getName());

        verify(repository).findWithRelationsById(1L);
    }


    @Test
    void shouldReturnLowStockStatusWhenFindingStockById() {
        Stock stock =
                createStock(
                        5
                );

        when(repository.findWithRelationsById(1L))
                .thenReturn(Optional.of(stock));

        var response =
                service.findById(1L);

        assertThat(response.lowStock()).isTrue();

        verify(repository).findWithRelationsById(1L);
    }

    @Test
    void shouldThrowWhenStockNotFound() {
        when(repository.findWithRelationsById(1L))
                .thenReturn(
                        Optional.empty()
                );

        var exception =
                assertThrows(StockNotFoundException.class,
                        () -> service.findById(1L));

        assertThat(exception.getErrorCode()).isEqualTo("STOCK_NOT_FOUND");

        verify(repository).findWithRelationsById(1L);
    }

    // Find All
    @ParameterizedTest
    @MethodSource("findAllFilters")
    void shouldFindAllStockWithFilters(Long productId, Long warehouseId) {
        Pageable pageable = PageRequest.of(0, 20);

        Stock stock = createStock(10);

        var page = new PageImpl<>(
                        List.of(stock),
                        pageable,
                        1
                );

        when(repository.findAllFiltered(productId, warehouseId, pageable))
                .thenReturn(page);

        var response = service.findAll(productId, warehouseId, pageable);

        assertThat(response).hasSize(1);
        assertThat(response.getContent().getFirst().id()).isEqualTo(1L);
        assertThat(response.getContent().getFirst().quantity()).isEqualTo(10);
        assertThat(response.getContent().getFirst().minimumStock()).isEqualTo(5);

        verify(repository).findAllFiltered(productId, warehouseId, pageable);
    }

    @Test
    void shouldReturnEmptyPageWhenNoStockMatchesFilters() {
        Pageable pageable = PageRequest.of(0, 20);

        when(repository.findAllFiltered(99L, null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var response = service.findAll(99L, null, pageable);

        assertThat(response).isEmpty();

        verify(repository).findAllFiltered(99L, null, pageable);
    }

    // Low Cost

    @ParameterizedTest
    @MethodSource("lowStockWarehouseFilters")
    void shouldFindLowStock(Long warehouseId) {
        Pageable pageable = PageRequest.of(0, 20);

        Stock stock = createStock(3);

        var page = new PageImpl<>(List.of(stock), pageable, 1);

        when(repository.findLowStock(warehouseId, pageable))
                .thenReturn(page);

        var response = service.findLowStock(warehouseId, pageable);

        assertThat(response).hasSize(1);

        var result = response.getContent().getFirst();

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.quantity()).isEqualTo(3);
        assertThat(result.minimumStock()).isEqualTo(5);
        assertThat(result.lowStock()).isTrue();

        verify(repository).findLowStock(warehouseId, pageable);
    }

    @Test
    void shouldReturnEmptyPageWhenThereIsNoLowStock() {
        Pageable pageable = PageRequest.of(0, 20);

        when(repository.findLowStock(null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var response = service.findLowStock(null, pageable);

        assertThat(response).isEmpty();

        verify(repository).findLowStock(null, pageable);
    }

    // Update minimum stock

    @Test
    void shouldUpdateMinimumStock() {
        Stock stock = createStock(10);

        StockMinimumRequest request = new StockMinimumRequest(8);

        when(repository.findWithRelationsById(1L))
                .thenReturn(Optional.of(stock));

        var response = service.updateMinimumStock(1L, request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.quantity()).isEqualTo(10);
        assertThat(response.minimumStock()).isEqualTo(8);
        assertThat(response.lowStock()).isFalse();
        assertThat(stock.getMinimumStock()).isEqualTo(8);

        verify(repository, never()).save(any(Stock.class));
        verify(repository).findWithRelationsById(1L);
    }

    @Test
    void shouldUpdateLowStockStatusAfterChangingMinimumStock() {
        Stock stock = createStock(10);

        StockMinimumRequest request = new StockMinimumRequest(15);

        when(repository.findWithRelationsById(1L))
                .thenReturn(Optional.of(stock));

        var response = service.updateMinimumStock(1L, request);

        assertThat(response.quantity()).isEqualTo(10);
        assertThat(response.minimumStock()).isEqualTo(15);
        assertThat(response.lowStock()).isTrue();
        assertThat(stock.isLowStock()).isTrue();

        verify(repository, never()).save(any(Stock.class));
    }

    @Test
    void shouldAllowZeroMinimumStock() {
        Stock stock = createStock(10);

        StockMinimumRequest request = new StockMinimumRequest(0);

        when(repository.findWithRelationsById(1L))
                .thenReturn(Optional.of(stock));

        var response = service.updateMinimumStock(1L, request);

        assertThat(response.minimumStock()).isZero();
        assertThat(stock.getMinimumStock()).isZero();
        assertThat(response.lowStock()).isFalse();

        verify(repository, never()).save(any(Stock.class));
    }

    @Test
    void shouldThrowWhenStockNotFoundForMinimumStockUpdate() {
        StockMinimumRequest request = new StockMinimumRequest(10);

        when(repository.findWithRelationsById(1L))
                .thenReturn(Optional.empty());

        var exception = assertThrows(StockNotFoundException.class,
                        () -> service.updateMinimumStock(1L, request));

        assertThat(exception.getErrorCode()).isEqualTo("STOCK_NOT_FOUND");

        verify(repository).findWithRelationsById(1L);
        verify(repository, never()).save(any(Stock.class));
    }
}