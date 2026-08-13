package com.burgosfacundo.inventory.inventory_movement.controller;

import com.burgosfacundo.inventory.common.config.WebConfig;
import com.burgosfacundo.inventory.common.exception.GlobalExceptionHandler;
import com.burgosfacundo.inventory.inventory_movement.dto.InventoryMovementRequest;
import com.burgosfacundo.inventory.inventory_movement.dto.InventoryMovementResponse;
import com.burgosfacundo.inventory.inventory_movement.exception.InvalidMovementDateRangeException;
import com.burgosfacundo.inventory.inventory_movement.exception.InventoryMovementNotFoundException;
import com.burgosfacundo.inventory.inventory_movement.model.MovementType;
import com.burgosfacundo.inventory.inventory_movement.service.InventoryMovementService;
import com.burgosfacundo.inventory.product.dto.ProductSummaryResponse;
import com.burgosfacundo.inventory.product.exception.ProductNotFoundException;
import com.burgosfacundo.inventory.stock.exception.InsufficientStockException;
import com.burgosfacundo.inventory.stock.exception.StockNotFoundException;
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
        controllers = InventoryMovementController.class,
        properties = "api.base-path=/api/v1"
)
@Import({
        WebConfig.class,
        GlobalExceptionHandler.class
})
class InventoryMovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryMovementService service;

    private InventoryMovementResponse response(MovementType type, int quantity) {
        return new InventoryMovementResponse(100L,
                new ProductSummaryResponse(1L, "SKU-001", "Product"),
                new WarehouseSummaryResponse(2L, "WH-001", "Main Warehouse"),
                type, quantity, LocalDateTime.of(2026, 8, 13, 16, 30));
    }

    static Stream<Arguments> movementTypes() {
        return Stream.of(
                Arguments.of(MovementType.IN),
                Arguments.of(MovementType.OUT)
        );
    }

    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                Arguments.of(
                        """
                        {
                          "warehouseId": 2,
                          "type": "IN",
                          "quantity": 10
                        }
                        """
                ),
                Arguments.of(
                        """
                        {
                          "productId": 1,
                          "type": "IN",
                          "quantity": 10
                        }
                        """
                ),
                Arguments.of(
                        """
                        {
                          "productId": 1,
                          "warehouseId": 2,
                          "quantity": 10
                        }
                        """
                ),
                Arguments.of(
                        """
                        {
                          "productId": 1,
                          "warehouseId": 2,
                          "type": "IN"
                        }
                        """
                ),
                Arguments.of(
                        """
                        {
                          "productId": 0,
                          "warehouseId": 2,
                          "type": "IN",
                          "quantity": 10
                        }
                        """
                ),
                Arguments.of(
                        """
                        {
                          "productId": 1,
                          "warehouseId": 0,
                          "type": "IN",
                          "quantity": 10
                        }
                        """
                ),
                Arguments.of(
                        """
                        {
                          "productId": 1,
                          "warehouseId": 2,
                          "type": "IN",
                          "quantity": 0
                        }
                        """
                ),
                Arguments.of(
                        """
                        {
                          "productId": 1,
                          "warehouseId": 2,
                          "type": "IN",
                          "quantity": -1
                        }
                        """
                )
        );
    }

    static Stream<Arguments> invalidPaginationParameters() {
        return Stream.of(
                Arguments.of("page", "-1"),
                Arguments.of("size", "0"),
                Arguments.of("size", "101")
        );
    }

    // Save

    @ParameterizedTest
    @MethodSource("movementTypes")
    void shouldCreateInventoryMovement(MovementType type) throws Exception {

        InventoryMovementRequest request = new InventoryMovementRequest(1L, 2L, type, 10);

        when(service.create(request))
                .thenReturn(response(type, 10));

        mockMvc.perform(post("/api/v1/inventory-movements")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.product.id").value(1))
                .andExpect(jsonPath("$.product.sku").value("SKU-001"))
                .andExpect(jsonPath("$.warehouse.id").value(2))
                .andExpect(jsonPath("$.warehouse.code").value("WH-001"))
                .andExpect(jsonPath("$.type").value(type.name()))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(service).create(request);
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void shouldReturnBadRequestWhenCreateRequestIsInvalid(String json) throws Exception {

        mockMvc.perform(post("/api/v1/inventory-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(service, never()).create(any(InventoryMovementRequest.class));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        InventoryMovementRequest request = new InventoryMovementRequest(99L, 2L, MovementType.IN, 10);

        when(service.create(request))
                .thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(post("/api/v1/inventory-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));

        verify(service).create(request);
    }

    @Test
    void shouldReturnNotFoundWhenWarehouseDoesNotExist() throws Exception {

        InventoryMovementRequest request = new InventoryMovementRequest(1L, 99L, MovementType.IN, 10);

        when(service.create(request))
                .thenThrow(new WarehouseNotFoundException(99L));

        mockMvc.perform(post("/api/v1/inventory-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("WAREHOUSE_NOT_FOUND"));

        verify(service).create(request);
    }

    @Test
    void shouldReturnNotFoundWhenOutMovementHasNoStock() throws Exception {

        InventoryMovementRequest request = new InventoryMovementRequest(1L, 2L, MovementType.OUT, 10);

        when(service.create(request))
                .thenThrow(new StockNotFoundException(1L, 2L));

        mockMvc.perform(post("/api/v1/inventory-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("STOCK_NOT_FOUND"));

        verify(service).create(request);
    }

    @Test
    void shouldReturnConflictWhenStockIsInsufficient() throws Exception {

        InventoryMovementRequest request = new InventoryMovementRequest(1L, 2L, MovementType.OUT, 15);


        when(service.create(request))
                .thenThrow(new InsufficientStockException(10, 15));

        mockMvc.perform(post("/api/v1/inventory-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_STOCK"));

        verify(service).create(request);
    }

    // Find By Id

    @Test
    void shouldFindMovementById() throws Exception {

        when(service.findById(100L))
                .thenReturn(response(MovementType.IN, 10));

        mockMvc.perform(get("/api/v1/inventory-movements/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.product.id").value(1))
                .andExpect(jsonPath("$.warehouse.id").value(2))
                .andExpect(jsonPath("$.type").value("IN"))
                .andExpect(jsonPath("$.quantity").value(10));

        verify(service).findById(100L);
    }

    @Test
    void shouldReturnNotFoundWhenMovementDoesNotExist() throws Exception {

        when(service.findById(999L))
                .thenThrow(new InventoryMovementNotFoundException(999L));

        mockMvc.perform(get("/api/v1/inventory-movements/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("MOVEMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.instance").value("/api/v1/inventory-movements/999"));

        verify(service).findById(999L);
    }

    @Test
    void shouldReturnBadRequestWhenMovementIdIsInvalid() throws Exception {

        mockMvc.perform(get("/api/v1/inventory-movements/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(service, never()).findById(anyLong());
    }


    // Find All

    @Test
    void shouldFindAllMovementsWithDefaultParameters() throws Exception {

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        var page = new PageImpl<>(List.of(response(MovementType.IN, 10)), pageable, 1);

        when(service.findAll(null, null,
                        null, null, null, pageable))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/inventory-movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).findAll(null, null, null,
                        null, null, pageable);
    }

    @Test
    void shouldFindMovementsWithFilters() throws Exception {

        LocalDateTime from = LocalDateTime.of(
                        2026,
                        8,
                        1,
                        0,
                        0);

        LocalDateTime to = LocalDateTime.of(
                        2026,
                        8,
                        31,
                        23,
                        59);

        Pageable pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "quantity"));

        var page = new PageImpl<>(List.of(response(MovementType.OUT, 5)), pageable, 11);

        when(service.findAll(1L, 2L, MovementType.OUT,
                        from, to, pageable))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/inventory-movements")
                        .param("productId", "1")
                        .param("warehouseId", "2")
                        .param("type", "OUT")
                        .param("from", "2026-08-01T00:00:00")
                        .param("to", "2026-08-31T23:59:00")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sortBy", "quantity")
                        .param("direction", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].type").value("OUT"))
                .andExpect(jsonPath("$.content[0].quantity").value(5))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(11));

        verify(service).findAll(1L, 2L, MovementType.OUT, from, to, pageable);
    }

    @Test
    void shouldReturnEmptyPageWhenFiltersMatchNoMovements() throws Exception {

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        when(service.findAll(999L, null, null,
                null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/api/v1/inventory-movements")
                        .param("productId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(service).findAll(999L, null, null, null, null, pageable);
    }

    @Test
    void shouldReturnBadRequestWhenProductFilterIsInvalid() throws Exception {

        mockMvc.perform(get("/api/v1/inventory-movements")
                        .param("productId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(service, never()).findAll(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnBadRequestWhenWarehouseFilterIsInvalid() throws Exception {

        mockMvc.perform(get("/api/v1/inventory-movements")
                        .param("warehouseId", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(service, never()).findAll(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @ParameterizedTest
    @MethodSource("invalidPaginationParameters")
    void shouldReturnBadRequestWhenPaginationIsInvalid(String parameter, String value) throws Exception {

        mockMvc.perform(get("/api/v1/inventory-movements")
                        .param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(service, never()).findAll(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnBadRequestWhenSortFieldIsInvalid() throws Exception {

        mockMvc.perform(get("/api/v1/inventory-movements")
                        .param("sortBy", "invalidField"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_SORT"))
                .andExpect(jsonPath("$.detail").value("Invalid sort field: invalidField"));

        verify(service, never()).findAll(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnBadRequestWhenSortDirectionIsInvalid() throws Exception {

        mockMvc.perform(get("/api/v1/inventory-movements")
                        .param("direction", "invalid"))
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

        when(service.findAll(null, null, null,
                        from, to, pageable))
                .thenThrow(new InvalidMovementDateRangeException());

        mockMvc.perform(get("/api/v1/inventory-movements")
                        .param("from", "2026-08-20T00:00:00")
                        .param("to", "2026-08-10T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_DATE_RANGE"))
                .andExpect(jsonPath("$.detail").value("'from' date cannot be after 'to' date"));

        verify(service).findAll(null, null, null,
                        from, to, pageable);
    }
}