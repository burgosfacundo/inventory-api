package com.burgosfacundo.inventory.stock.controller;

import com.burgosfacundo.inventory.common.config.WebConfig;
import com.burgosfacundo.inventory.common.exception.GlobalExceptionHandler;
import com.burgosfacundo.inventory.product.dto.ProductSummaryResponse;
import com.burgosfacundo.inventory.stock.StockController;
import com.burgosfacundo.inventory.stock.dto.StockMinimumRequest;
import com.burgosfacundo.inventory.stock.dto.StockResponse;
import com.burgosfacundo.inventory.stock.exception.StockNotFoundException;
import com.burgosfacundo.inventory.stock.service.StockService;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = StockController.class,
        properties = "api.base-path=/api/v1"
)
@Import({
        WebConfig.class,
        GlobalExceptionHandler.class
})
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StockService service;

    private StockResponse response() {
        return new StockResponse(
                1L,
                new ProductSummaryResponse(
                        10L,
                        "SKU-001",
                        "Product"
                ),
                new WarehouseSummaryResponse(
                        20L,
                        "WH-001",
                        "Main Warehouse"
                ),
                10,
                5,
                false
        );
    }

    private StockResponse lowStockResponse() {
        return new StockResponse(
                2L,
                new ProductSummaryResponse(
                        11L,
                        "SKU-002",
                        "Low Stock Product"
                ),
                new WarehouseSummaryResponse(
                        20L,
                        "WH-001",
                        "Main Warehouse"
                ),
                3,
                5,
                true
        );
    }

    static Stream<Arguments> invalidPaginationParameters() {
        return Stream.of(
                Arguments.of("page", "-1"),
                Arguments.of("size", "0"),
                Arguments.of("size", "101")
        );
    }


    @Test
    void shouldFindStockById() throws Exception {
        when(service.findById(1L))
                .thenReturn(response());

        mockMvc.perform(
                        get("/api/v1/stocks/1")
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentType(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.product.id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.product.sku")
                                .value("SKU-001")
                )
                .andExpect(
                        jsonPath("$.product.name")
                                .value("Product")
                )
                .andExpect(
                        jsonPath("$.warehouse.id")
                                .value(20)
                )
                .andExpect(
                        jsonPath("$.warehouse.code")
                                .value("WH-001")
                )
                .andExpect(
                        jsonPath("$.warehouse.name")
                                .value("Main Warehouse")
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.minimumStock")
                                .value(5)
                )
                .andExpect(
                        jsonPath("$.lowStock")
                                .value(false)
                );

        verify(service).findById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenStockDoesNotExist()
            throws Exception {

        when(service.findById(99L))
                .thenThrow(
                        new StockNotFoundException(99L)
                );

        mockMvc.perform(
                        get("/api/v1/stocks/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("STOCK_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.instance")
                                .value("/api/v1/stocks/99")
                );

        verify(service).findById(99L);
    }

    @Test
    void shouldReturnBadRequestWhenStockIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/stocks/0")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );

        verify(service, never())
                .findById(anyLong());
    }


    @Test
    void shouldFindAllStocksWithDefaultParameters()
            throws Exception {

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Direction.ASC,
                                "id"
                        )
                );

        var page =
                new PageImpl<>(
                        List.of(response()),
                        pageable,
                        1
                );

        when(service.findAll(
                null,
                null,
                pageable
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/stocks")
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentType(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.content")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].quantity")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.number")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.size")
                                .value(20)
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                );

        verify(service)
                .findAll(
                        null,
                        null,
                        pageable
                );
    }

    @Test
    void shouldFilterStocksByProduct()
            throws Exception {

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Direction.ASC,
                                "id"
                        )
                );

        var page =
                new PageImpl<>(
                        List.of(response()),
                        pageable,
                        1
                );

        when(service.findAll(
                10L,
                null,
                pageable
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/stocks")
                                .param(
                                        "productId",
                                        "10"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].product.id"
                        ).value(10)
                );

        verify(service)
                .findAll(
                        10L,
                        null,
                        pageable
                );
    }

    @Test
    void shouldFilterStocksByWarehouse()
            throws Exception {

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Direction.ASC,
                                "id"
                        )
                );

        var page =
                new PageImpl<>(
                        List.of(response()),
                        pageable,
                        1
                );

        when(service.findAll(
                null,
                20L,
                pageable
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/stocks")
                                .param(
                                        "warehouseId",
                                        "20"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.content[0].warehouse.id"
                        ).value(20)
                );

        verify(service)
                .findAll(
                        null,
                        20L,
                        pageable
                );
    }

    @Test
    void shouldFilterStocksByProductAndWarehouse()
            throws Exception {

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Direction.ASC,
                                "id"
                        )
                );

        var page =
                new PageImpl<>(
                        List.of(response()),
                        pageable,
                        1
                );

        when(service.findAll(
                10L,
                20L,
                pageable
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/stocks")
                                .param(
                                        "productId",
                                        "10"
                                )
                                .param(
                                        "warehouseId",
                                        "20"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].product.id"
                        ).value(10)
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].warehouse.id"
                        ).value(20)
                );

        verify(service)
                .findAll(
                        10L,
                        20L,
                        pageable
                );
    }

    @Test
    void shouldFindStocksWithCustomPaginationAndSorting()
            throws Exception {

        Pageable pageable =
                PageRequest.of(
                        1,
                        10,
                        Sort.by(
                                Sort.Direction.DESC,
                                "quantity"
                        )
                );

        var page =
                new PageImpl<>(
                        List.of(response()),
                        pageable,
                        11
                );

        when(service.findAll(
                null,
                null,
                pageable
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/stocks")
                                .param("page", "1")
                                .param("size", "10")
                                .param(
                                        "sortBy",
                                        "quantity"
                                )
                                .param(
                                        "direction",
                                        "desc"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.number")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.size")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(11)
                );

        verify(service)
                .findAll(
                        null,
                        null,
                        pageable
                );
    }

    @Test
    void shouldReturnBadRequestWhenProductFilterIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/stocks")
                                .param(
                                        "productId",
                                        "0"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );

        verify(service, never())
                .findAll(
                        any(),
                        any(),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldReturnBadRequestWhenWarehouseFilterIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/stocks")
                                .param(
                                        "warehouseId",
                                        "-1"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );

        verify(service, never())
                .findAll(
                        any(),
                        any(),
                        any(Pageable.class)
                );
    }

    @ParameterizedTest
    @MethodSource("invalidPaginationParameters")
    void shouldReturnBadRequestWhenFindAllPaginationIsInvalid(
            String parameter,
            String value
    ) throws Exception {

        mockMvc.perform(
                        get("/api/v1/stocks")
                                .param(parameter, value)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );

        verify(service, never())
                .findAll(
                        any(),
                        any(),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldReturnBadRequestWhenSortFieldIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/stocks")
                                .param(
                                        "sortBy",
                                        "invalidField"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().contentType(
                                MediaType.APPLICATION_PROBLEM_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("INVALID_SORT")
                )
                .andExpect(
                        jsonPath("$.detail")
                                .value(
                                        "Invalid sort field: invalidField"
                                )
                );

        verify(service, never())
                .findAll(
                        any(),
                        any(),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldReturnBadRequestWhenSortDirectionIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/stocks")
                                .param(
                                        "direction",
                                        "invalid"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("INVALID_SORT")
                )
                .andExpect(
                        jsonPath("$.detail")
                                .value(
                                        "Invalid sort direction: invalid"
                                )
                );

        verify(service, never())
                .findAll(
                        any(),
                        any(),
                        any(Pageable.class)
                );
    }


    @Test
    void shouldFindLowStockWithDefaultParameters()
            throws Exception {

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Direction.ASC,
                                "id"
                        )
                );

        var page =
                new PageImpl<>(
                        List.of(lowStockResponse()),
                        pageable,
                        1
                );

        when(service.findLowStock(
                null,
                pageable
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/stocks/low-stock")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].quantity")
                                .value(3)
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].minimumStock"
                        ).value(5)
                )
                .andExpect(
                        jsonPath("$.content[0].lowStock")
                                .value(true)
                );

        verify(service)
                .findLowStock(
                        null,
                        pageable
                );
    }

    @Test
    void shouldFilterLowStockByWarehouse()
            throws Exception {

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Direction.ASC,
                                "id"
                        )
                );

        var page =
                new PageImpl<>(
                        List.of(lowStockResponse()),
                        pageable,
                        1
                );

        when(service.findLowStock(
                20L,
                pageable
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/stocks/low-stock")
                                .param(
                                        "warehouseId",
                                        "20"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.content[0].warehouse.id"
                        ).value(20)
                )
                .andExpect(
                        jsonPath("$.content[0].lowStock")
                                .value(true)
                );

        verify(service)
                .findLowStock(
                        20L,
                        pageable
                );
    }

    @Test
    void shouldFindLowStockWithCustomPaginationAndSorting()
            throws Exception {

        Pageable pageable =
                PageRequest.of(
                        1,
                        10,
                        Sort.by(
                                Sort.Direction.ASC,
                                "minimumStock"
                        )
                );

        var page =
                new PageImpl<>(
                        List.of(lowStockResponse()),
                        pageable,
                        11
                );

        when(service.findLowStock(
                null,
                pageable
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/stocks/low-stock")
                                .param("page", "1")
                                .param("size", "10")
                                .param(
                                        "sortBy",
                                        "minimumStock"
                                )
                                .param(
                                        "direction",
                                        "asc"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.number")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.size")
                                .value(10)
                );

        verify(service)
                .findLowStock(
                        null,
                        pageable
                );
    }

    @Test
    void shouldReturnBadRequestWhenLowStockWarehouseFilterIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/stocks/low-stock")
                                .param(
                                        "warehouseId",
                                        "0"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );

        verify(service, never())
                .findLowStock(
                        any(),
                        any(Pageable.class)
                );
    }

    @ParameterizedTest
    @MethodSource("invalidPaginationParameters")
    void shouldReturnBadRequestWhenLowStockPaginationIsInvalid(
            String parameter,
            String value
    ) throws Exception {

        mockMvc.perform(
                        get("/api/v1/stocks/low-stock")
                                .param(parameter, value)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );

        verify(service, never())
                .findLowStock(
                        any(),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldReturnBadRequestWhenLowStockSortFieldIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/stocks/low-stock")
                                .param(
                                        "sortBy",
                                        "product.name"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("INVALID_SORT")
                );

        verify(service, never())
                .findLowStock(
                        any(),
                        any(Pageable.class)
                );
    }


    @Test
    void shouldUpdateMinimumStock()
            throws Exception {

        StockMinimumRequest request =
                new StockMinimumRequest(8);

        StockResponse response =
                new StockResponse(
                        1L,
                        new ProductSummaryResponse(
                                10L,
                                "SKU-001",
                                "Product"
                        ),
                        new WarehouseSummaryResponse(
                                20L,
                                "WH-001",
                                "Main Warehouse"
                        ),
                        10,
                        8,
                        false
                );

        when(service.updateMinimumStock(
                1L,
                request
        )).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/v1/stocks/1/minimum-stock"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentType(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.minimumStock")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$.lowStock")
                                .value(false)
                );

        verify(service)
                .updateMinimumStock(
                        1L,
                        request
                );
    }

    @Test
    void shouldAllowZeroMinimumStock()
            throws Exception {

        StockMinimumRequest request =
                new StockMinimumRequest(0);

        StockResponse response =
                new StockResponse(
                        1L,
                        new ProductSummaryResponse(
                                10L,
                                "SKU-001",
                                "Product"
                        ),
                        new WarehouseSummaryResponse(
                                20L,
                                "WH-001",
                                "Main Warehouse"
                        ),
                        10,
                        0,
                        false
                );

        when(service.updateMinimumStock(
                1L,
                request
        )).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/v1/stocks/1/minimum-stock"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.minimumStock")
                                .value(0)
                );

        verify(service)
                .updateMinimumStock(
                        1L,
                        request
                );
    }

    @Test
    void shouldReturnBadRequestWhenMinimumStockIsNegative()
            throws Exception {

        StockMinimumRequest request =
                new StockMinimumRequest(-1);

        mockMvc.perform(
                        patch(
                                "/api/v1/stocks/1/minimum-stock"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().contentType(
                                MediaType.APPLICATION_PROBLEM_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );

        verify(service, never())
                .updateMinimumStock(
                        anyLong(),
                        any(StockMinimumRequest.class)
                );
    }

    @Test
    void shouldReturnBadRequestWhenMinimumStockIsNull()
            throws Exception {

        StockMinimumRequest request =
                new StockMinimumRequest(null);

        mockMvc.perform(
                        patch(
                                "/api/v1/stocks/1/minimum-stock"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );

        verify(service, never())
                .updateMinimumStock(
                        anyLong(),
                        any(StockMinimumRequest.class)
                );
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingStock()
            throws Exception {

        StockMinimumRequest request =
                new StockMinimumRequest(10);

        when(service.updateMinimumStock(
                99L,
                request
        )).thenThrow(
                new StockNotFoundException(99L)
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/stocks/99/minimum-stock"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("STOCK_NOT_FOUND")
                );

        verify(service)
                .updateMinimumStock(
                        99L,
                        request
                );
    }

    @Test
    void shouldReturnBadRequestWhenMinimumStockUpdateIdIsInvalid()
            throws Exception {

        StockMinimumRequest request =
                new StockMinimumRequest(10);

        mockMvc.perform(
                        patch(
                                "/api/v1/stocks/0/minimum-stock"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );

        verify(service, never())
                .updateMinimumStock(
                        anyLong(),
                        any(StockMinimumRequest.class)
                );
    }
}