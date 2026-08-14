package com.burgosfacundo.inventory.stock_transfer.controller;

import com.burgosfacundo.inventory.common.config.WebConfig;
import com.burgosfacundo.inventory.common.exception.GlobalExceptionHandler;
import com.burgosfacundo.inventory.product.dto.ProductSummaryResponse;
import com.burgosfacundo.inventory.product.exception.ProductNotFoundException;
import com.burgosfacundo.inventory.stock.exception.InsufficientStockException;
import com.burgosfacundo.inventory.stock.exception.StockNotFoundException;
import com.burgosfacundo.inventory.stock_transfer.dto.StockTransferRequest;
import com.burgosfacundo.inventory.stock_transfer.dto.StockTransferResponse;
import com.burgosfacundo.inventory.stock_transfer.exception.InvalidTransferDateRangeException;
import com.burgosfacundo.inventory.stock_transfer.exception.SameWarehouseTransferException;
import com.burgosfacundo.inventory.stock_transfer.exception.StockTransferNotFoundException;
import com.burgosfacundo.inventory.stock_transfer.service.StockTransferService;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseSummaryResponse;
import com.burgosfacundo.inventory.warehouse.exception.WarehouseNotFoundException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = StockTransferController.class,
        properties = "api.base-path=/api/v1"
)
@Import({WebConfig.class, GlobalExceptionHandler.class})
class StockTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StockTransferService service;

    private StockTransferResponse response() {
        return new StockTransferResponse(
                300L,
                new ProductSummaryResponse(1L, "SKU-001", "Product"),
                new WarehouseSummaryResponse(10L, "WH-001", "Source Warehouse"),
                new WarehouseSummaryResponse(20L, "WH-002", "Destination Warehouse"),
                8,
                LocalDateTime.of(2026, 8, 14, 12, 0)
        );
    }

    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                Arguments.of("""
                        {
                          "sourceWarehouseId": 10,
                          "destinationWarehouseId": 20,
                          "quantity": 8
                        }
                        """),
                Arguments.of("""
                        {
                          "productId": 1,
                          "destinationWarehouseId": 20,
                          "quantity": 8
                        }
                        """),
                Arguments.of("""
                        {
                          "productId": 1,
                          "sourceWarehouseId": 10,
                          "quantity": 8
                        }
                        """),
                Arguments.of("""
                        {
                          "productId": 1,
                          "sourceWarehouseId": 10,
                          "destinationWarehouseId": 20
                        }
                        """),
                Arguments.of("""
                        {
                          "productId": 0,
                          "sourceWarehouseId": 10,
                          "destinationWarehouseId": 20,
                          "quantity": 8
                        }
                        """),
                Arguments.of("""
                        {
                          "productId": 1,
                          "sourceWarehouseId": 0,
                          "destinationWarehouseId": 20,
                          "quantity": 8
                        }
                        """),
                Arguments.of("""
                        {
                          "productId": 1,
                          "sourceWarehouseId": 10,
                          "destinationWarehouseId": 0,
                          "quantity": 8
                        }
                        """),
                Arguments.of("""
                        {
                          "productId": 1,
                          "sourceWarehouseId": 10,
                          "destinationWarehouseId": 20,
                          "quantity": 0
                        }
                        """),
                Arguments.of("""
                        {
                          "productId": 1,
                          "sourceWarehouseId": 10,
                          "destinationWarehouseId": 20,
                          "quantity": -1
                        }
                        """)
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
    void shouldCreateStockTransfer() throws Exception {
        StockTransferRequest request = new StockTransferRequest(1L, 10L, 20L, 8);

        when(service.create(request)).thenReturn(response());

        mockMvc.perform(post("/api/v1/stock-transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(300))
                .andExpect(jsonPath("$.product.id").value(1))
                .andExpect(jsonPath("$.product.sku").value("SKU-001"))
                .andExpect(jsonPath("$.sourceWarehouse.id").value(10))
                .andExpect(jsonPath("$.sourceWarehouse.code").value("WH-001"))
                .andExpect(jsonPath("$.destinationWarehouse.id").value(20))
                .andExpect(jsonPath("$.destinationWarehouse.code").value("WH-002"))
                .andExpect(jsonPath("$.quantity").value(8))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(service).create(request);
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void shouldReturnBadRequestWhenCreateRequestIsInvalid(String json) throws Exception {
        mockMvc.perform(post("/api/v1/stock-transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(service, never()).create(any(StockTransferRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenWarehousesAreTheSame() throws Exception {
        StockTransferRequest request = new StockTransferRequest(1L, 10L, 10L, 8);

        when(service.create(request)).thenThrow(new SameWarehouseTransferException());

        mockMvc.perform(post("/api/v1/stock-transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("SAME_WAREHOUSE_TRANSFER"));

        verify(service).create(request);
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        StockTransferRequest request = new StockTransferRequest(99L, 10L, 20L, 8);

        when(service.create(request)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(post("/api/v1/stock-transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));

        verify(service).create(request);
    }

    @Test
    void shouldReturnNotFoundWhenWarehouseDoesNotExist() throws Exception {
        StockTransferRequest request = new StockTransferRequest(1L, 99L, 20L, 8);

        when(service.create(request)).thenThrow(new WarehouseNotFoundException(99L));

        mockMvc.perform(post("/api/v1/stock-transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("WAREHOUSE_NOT_FOUND"));

        verify(service).create(request);
    }

    @Test
    void shouldReturnNotFoundWhenSourceStockDoesNotExist() throws Exception {
        StockTransferRequest request = new StockTransferRequest(1L, 10L, 20L, 8);

        when(service.create(request)).thenThrow(new StockNotFoundException(1L, 10L));

        mockMvc.perform(post("/api/v1/stock-transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("STOCK_NOT_FOUND"));

        verify(service).create(request);
    }

    @Test
    void shouldReturnConflictWhenSourceStockIsInsufficient() throws Exception {
        StockTransferRequest request = new StockTransferRequest(1L, 10L, 20L, 8);

        when(service.create(request)).thenThrow(new InsufficientStockException(5, 8));

        mockMvc.perform(post("/api/v1/stock-transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_STOCK"));

        verify(service).create(request);
    }


    @Test
    void shouldFindStockTransferById() throws Exception {
        when(service.findById(300L)).thenReturn(response());

        mockMvc.perform(get("/api/v1/stock-transfers/300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(300))
                .andExpect(jsonPath("$.product.id").value(1))
                .andExpect(jsonPath("$.sourceWarehouse.id").value(10))
                .andExpect(jsonPath("$.destinationWarehouse.id").value(20))
                .andExpect(jsonPath("$.quantity").value(8));

        verify(service).findById(300L);
    }

    @Test
    void shouldReturnNotFoundWhenStockTransferDoesNotExist() throws Exception {
        when(service.findById(999L)).thenThrow(new StockTransferNotFoundException(999L));

        mockMvc.perform(get("/api/v1/stock-transfers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("STOCK_TRANSFER_NOT_FOUND"))
                .andExpect(jsonPath("$.instance").value("/api/v1/stock-transfers/999"));

        verify(service).findById(999L);
    }

    @Test
    void shouldReturnBadRequestWhenStockTransferIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/stock-transfers/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(service, never()).findById(anyLong());
    }


    @Test
    void shouldFindAllStockTransfersWithDefaultParameters() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        var page = new PageImpl<>(List.of(response()), pageable, 1);

        when(service.findAll(null, null, null, null, null, pageable)).thenReturn(page);

        mockMvc.perform(get("/api/v1/stock-transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(300))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).findAll(null, null, null, null, null, pageable);
    }

    @Test
    void shouldFindStockTransfersWithFilters() throws Exception {
        LocalDateTime from = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 8, 31, 23, 59);

        Pageable pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "quantity"));
        var page = new PageImpl<>(List.of(response()), pageable, 11);

        when(service.findAll(1L, 10L, 20L, from, to, pageable)).thenReturn(page);

        mockMvc.perform(get("/api/v1/stock-transfers")
                        .param("productId", "1")
                        .param("sourceWarehouseId", "10")
                        .param("destinationWarehouseId", "20")
                        .param("from", "2026-08-01T00:00:00")
                        .param("to", "2026-08-31T23:59:00")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sortBy", "quantity")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].product.id").value(1))
                .andExpect(jsonPath("$.content[0].sourceWarehouse.id").value(10))
                .andExpect(jsonPath("$.content[0].destinationWarehouse.id").value(20))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(11));

        verify(service).findAll(1L, 10L, 20L, from, to, pageable);
    }

    @Test
    void shouldReturnEmptyPageWhenFiltersMatchNoTransfers() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        when(service.findAll(999L, null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/api/v1/stock-transfers").param("productId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(service).findAll(999L, null, null, null, null, pageable);
    }

    @Test
    void shouldReturnBadRequestWhenProductFilterIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/stock-transfers").param("productId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(service, never()).findAll(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnBadRequestWhenSourceWarehouseFilterIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/stock-transfers").param("sourceWarehouseId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(service, never()).findAll(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnBadRequestWhenDestinationWarehouseFilterIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/stock-transfers").param("destinationWarehouseId", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(service, never()).findAll(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @ParameterizedTest
    @MethodSource("invalidPaginationParameters")
    void shouldReturnBadRequestWhenPaginationIsInvalid(String parameter, String value) throws Exception {
        mockMvc.perform(get("/api/v1/stock-transfers").param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(service, never()).findAll(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnBadRequestWhenSortFieldIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/stock-transfers").param("sortBy", "invalidField"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_SORT"))
                .andExpect(jsonPath("$.detail").value("Invalid sort field: invalidField"));

        verify(service, never()).findAll(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnBadRequestWhenSortDirectionIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/stock-transfers").param("direction", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_SORT"))
                .andExpect(jsonPath("$.detail").value("Invalid sort direction: invalid"));

        verify(service, never()).findAll(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnBadRequestWhenDateRangeIsInvalid() throws Exception {
        LocalDateTime from = LocalDateTime.of(2026, 8, 20, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 8, 10, 0, 0);

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        when(service.findAll(null, null, null, from, to, pageable))
                .thenThrow(new InvalidTransferDateRangeException());

        mockMvc.perform(get("/api/v1/stock-transfers")
                        .param("from", "2026-08-20T00:00:00")
                        .param("to", "2026-08-10T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_DATE_RANGE"))
                .andExpect(jsonPath("$.detail").value("'from' date cannot be after 'to' date"));

        verify(service).findAll(null, null, null, from, to, pageable);
    }
}