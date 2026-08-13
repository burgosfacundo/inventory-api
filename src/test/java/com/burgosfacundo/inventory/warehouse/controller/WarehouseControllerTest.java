package com.burgosfacundo.inventory.warehouse.controller;

import com.burgosfacundo.inventory.common.config.WebConfig;
import com.burgosfacundo.inventory.common.exception.GlobalExceptionHandler;
import com.burgosfacundo.inventory.warehouse.dto.AddressRequest;
import com.burgosfacundo.inventory.warehouse.dto.AddressResponse;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseRequest;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseResponse;
import com.burgosfacundo.inventory.warehouse.exception.AddressInvalidException;
import com.burgosfacundo.inventory.warehouse.exception.AddressNotFoundException;
import com.burgosfacundo.inventory.warehouse.exception.AddressProviderUnavailableException;
import com.burgosfacundo.inventory.warehouse.exception.WarehouseCodeAlreadyExistsException;
import com.burgosfacundo.inventory.warehouse.exception.WarehouseNotFoundException;
import com.burgosfacundo.inventory.warehouse.service.WarehouseService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = WarehouseController.class,
        properties = "api.base-path=/api/v1"
)
@Import({
        WebConfig.class,
        GlobalExceptionHandler.class
})
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WarehouseService service;

    static Stream<Arguments> invalidFindAllParameters() {
        return Stream.of(
                Arguments.of("page", "-1"),
                Arguments.of("size", "0"),
                Arguments.of("size", "101")
        );
    }

    private AddressRequest addressRequest() {
        return new AddressRequest(
                "Av. Independencia",
                "1234",
                "7600",
                "Mar del Plata",
                "Buenos Aires",
                "AR"
        );
    }

    private WarehouseRequest request() {
        return new WarehouseRequest(
                "WH-001",
                "Main Warehouse",
                addressRequest()
        );
    }

    private AddressResponse addressResponse() {
        return new AddressResponse(
                "Avenida Independencia",
                "1234",
                "B7600",
                "Mar del Plata",
                "Buenos Aires",
                "AR",
                new BigDecimal("-38.0055"),
                new BigDecimal("-57.5426")
        );
    }

    private WarehouseResponse response() {
        return new WarehouseResponse(
                1L,
                "WH-001",
                "Main Warehouse",
                addressResponse()
        );
    }

    @Test
    void shouldSaveWarehouse() throws Exception {
        WarehouseRequest request = request();
        WarehouseResponse response = response();

        when(service.save(request))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/warehouses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        content().contentType(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.code")
                                .value("WH-001")
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Main Warehouse")
                )
                .andExpect(
                        jsonPath("$.address.street")
                                .value("Avenida Independencia")
                )
                .andExpect(
                        jsonPath("$.address.number")
                                .value("1234")
                )
                .andExpect(
                        jsonPath("$.address.postalCode")
                                .value("B7600")
                )
                .andExpect(
                        jsonPath("$.address.city")
                                .value("Mar del Plata")
                )
                .andExpect(
                        jsonPath("$.address.province")
                                .value("Buenos Aires")
                )
                .andExpect(
                        jsonPath("$.address.countryCode")
                                .value("AR")
                )
                .andExpect(
                        jsonPath("$.address.latitude")
                                .value(-38.0055)
                )
                .andExpect(
                        jsonPath("$.address.longitude")
                                .value(-57.5426)
                );

        verify(service).save(request);
    }

    @Test
    void shouldReturnBadRequestWhenCodeIsBlank()
            throws Exception {

        WarehouseRequest request =
                new WarehouseRequest(
                        "",
                        "Main Warehouse",
                        addressRequest()
                );

        mockMvc.perform(
                        post("/api/v1/warehouses")
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
                .save(any(WarehouseRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank()
            throws Exception {

        WarehouseRequest request =
                new WarehouseRequest(
                        "WH-001",
                        "",
                        addressRequest()
                );

        mockMvc.perform(
                        post("/api/v1/warehouses")
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
                .save(any(WarehouseRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenAddressIsNull()
            throws Exception {

        WarehouseRequest request =
                new WarehouseRequest(
                        "WH-001",
                        "Main Warehouse",
                        null
                );

        mockMvc.perform(
                        post("/api/v1/warehouses")
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
                .save(any(WarehouseRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenNestedAddressIsInvalid()
            throws Exception {

        AddressRequest invalidAddress =
                new AddressRequest(
                        "",
                        "",
                        "7600",
                        "Mar del Plata",
                        "Buenos Aires",
                        "AR"
                );

        WarehouseRequest request =
                new WarehouseRequest(
                        "WH-001",
                        "Main Warehouse",
                        invalidAddress
                );

        mockMvc.perform(
                        post("/api/v1/warehouses")
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
                .save(any(WarehouseRequest.class));
    }

    @Test
    void shouldReturnConflictWhenWarehouseCodeAlreadyExists()
            throws Exception {

        WarehouseRequest request = request();

        when(service.save(request))
                .thenThrow(
                        new WarehouseCodeAlreadyExistsException(
                                request.code()
                        )
                );

        mockMvc.perform(
                        post("/api/v1/warehouses")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        content().contentType(
                                MediaType.APPLICATION_PROBLEM_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(
                                        "WAREHOUSE_CODE_ALREADY_EXISTS"
                                )
                );

        verify(service).save(request);
    }

    @Test
    void shouldReturnUnprocessableContentWhenAddressCannotBeResolved()
            throws Exception {

        WarehouseRequest request = request();

        when(service.save(request))
                .thenThrow(
                        new AddressNotFoundException()
                );

        mockMvc.perform(
                        post("/api/v1/warehouses")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isUnprocessableContent()
                )
                .andExpect(
                        content().contentType(
                                MediaType.APPLICATION_PROBLEM_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("ADDRESS_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.instance")
                                .value("/api/v1/warehouses")
                );

        verify(service).save(request);
    }

    @Test
    void shouldReturnServiceUnavailableWhenAddressProviderFails()
            throws Exception {

        WarehouseRequest request = request();

        when(service.save(request))
                .thenThrow(
                        new AddressProviderUnavailableException()
                );

        mockMvc.perform(
                        post("/api/v1/warehouses")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isServiceUnavailable()
                )
                .andExpect(
                        content().contentType(
                                MediaType.APPLICATION_PROBLEM_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(
                                        "ADDRESS_PROVIDER_UNAVAILABLE"
                                )
                );

        verify(service).save(request);
    }


    @Test
    void shouldFindWarehouseById()
            throws Exception {

        WarehouseResponse response = response();

        when(service.findById(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/warehouses/1")
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentType(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.code")
                                .value("WH-001")
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Main Warehouse")
                )
                .andExpect(
                        jsonPath("$.address.city")
                                .value("Mar del Plata")
                );

        verify(service).findById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenWarehouseDoesNotExist()
            throws Exception {

        when(service.findById(99L))
                .thenThrow(
                        new WarehouseNotFoundException(99L)
                );

        mockMvc.perform(
                        get("/api/v1/warehouses/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("WAREHOUSE_NOT_FOUND")
                );

        verify(service).findById(99L);
    }

    @Test
    void shouldReturnBadRequestWhenFindByIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/warehouses/0")
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
    void shouldFindAllWarehousesWithDefaultParameters()
            throws Exception {

        WarehouseResponse response = response();

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Direction.ASC,
                                "id"
                        )
                );

        var result =
                new PageImpl<>(
                        List.of(response),
                        pageable,
                        1
                );

        when(service.findAll(pageable))
                .thenReturn(result);

        mockMvc.perform(
                        get("/api/v1/warehouses")
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
                        jsonPath("$.content[0].code")
                                .value("WH-001")
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

        verify(service).findAll(pageable);
    }

    @Test
    void shouldFindAllWarehousesWithPaginationAndSorting()
            throws Exception {

        WarehouseResponse response = response();

        Pageable pageable =
                PageRequest.of(
                        1,
                        10,
                        Sort.by(
                                Sort.Direction.DESC,
                                "code"
                        )
                );

        var result =
                new PageImpl<>(
                        List.of(response),
                        pageable,
                        11
                );

        when(service.findAll(pageable))
                .thenReturn(result);

        mockMvc.perform(
                        get("/api/v1/warehouses")
                                .param("page", "1")
                                .param("size", "10")
                                .param("sortBy", "code")
                                .param("direction", "desc")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
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

        verify(service).findAll(pageable);
    }

    @Test
    void shouldReturnBadRequestWhenSortFieldIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/warehouses")
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
                .findAll(any(Pageable.class));
    }

    @Test
    void shouldReturnBadRequestWhenSortDirectionIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/warehouses")
                                .param("sortBy", "code")
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
                .findAll(any(Pageable.class));
    }

    @ParameterizedTest
    @MethodSource("invalidFindAllParameters")
    void shouldReturnBadRequestWhenFindAllParametersAreInvalid(
            String parameter,
            String value
    ) throws Exception {

        mockMvc.perform(
                        get("/api/v1/warehouses")
                                .param(parameter, value)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );

        verify(service, never())
                .findAll(any(Pageable.class));
    }


    @Test
    void shouldUpdateWarehouse()
            throws Exception {

        WarehouseRequest request = request();
        WarehouseResponse response = response();

        when(service.update(1L, request))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/warehouses/1")
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
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.code")
                                .value("WH-001")
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Main Warehouse")
                )
                .andExpect(
                        jsonPath("$.address.street")
                                .value(
                                        "Avenida Independencia"
                                )
                );

        verify(service).update(1L, request);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingWarehouse()
            throws Exception {

        WarehouseRequest request = request();

        when(service.update(99L, request))
                .thenThrow(
                        new WarehouseNotFoundException(99L)
                );

        mockMvc.perform(
                        put("/api/v1/warehouses/99")
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
                        jsonPath("$.errorCode")
                                .value("WAREHOUSE_NOT_FOUND")
                );

        verify(service).update(99L, request);
    }

    @Test
    void shouldReturnConflictWhenUpdatingWithExistingCode()
            throws Exception {

        WarehouseRequest request = request();

        when(service.update(1L, request))
                .thenThrow(
                        new WarehouseCodeAlreadyExistsException(
                                request.code()
                        )
                );

        mockMvc.perform(
                        put("/api/v1/warehouses/1")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(
                                        "WAREHOUSE_CODE_ALREADY_EXISTS"
                                )
                );

        verify(service).update(1L, request);
    }

    @Test
    void shouldReturnUnprocessableContentWhenUpdatedAddressIsInvalid()
            throws Exception {

        WarehouseRequest request = request();

        when(service.update(1L, request))
                .thenThrow(
                        new AddressInvalidException()
                );

        mockMvc.perform(
                        put("/api/v1/warehouses/1")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isUnprocessableContent()
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("ADDRESS_INVALID")
                );

        verify(service).update(1L, request);
    }

    @Test
    void shouldReturnServiceUnavailableWhenProviderFailsDuringUpdate()
            throws Exception {

        WarehouseRequest request = request();

        when(service.update(1L, request))
                .thenThrow(
                        new AddressProviderUnavailableException()
                );

        mockMvc.perform(
                        put("/api/v1/warehouses/1")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isServiceUnavailable()
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(
                                        "ADDRESS_PROVIDER_UNAVAILABLE"
                                )
                );

        verify(service).update(1L, request);
    }

    @Test
    void shouldReturnBadRequestWhenUpdateBodyIsInvalid()
            throws Exception {

        WarehouseRequest request =
                new WarehouseRequest(
                        "",
                        "",
                        null
                );

        mockMvc.perform(
                        put("/api/v1/warehouses/1")
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
                .update(
                        anyLong(),
                        any(WarehouseRequest.class)
                );
    }

    @Test
    void shouldReturnBadRequestWhenUpdateIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                        put("/api/v1/warehouses/0")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request()
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );

        verify(service, never())
                .update(
                        anyLong(),
                        any(WarehouseRequest.class)
                );
    }


    @Test
    void shouldDeleteWarehouse()
            throws Exception {

        doNothing()
                .when(service)
                .delete(1L);

        mockMvc.perform(
                        delete("/api/v1/warehouses/1")
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).delete(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingWarehouse()
            throws Exception {

        doThrow(
                new WarehouseNotFoundException(99L)
        )
                .when(service)
                .delete(99L);

        mockMvc.perform(
                        delete("/api/v1/warehouses/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("WAREHOUSE_NOT_FOUND")
                );

        verify(service).delete(99L);
    }

    @Test
    void shouldReturnBadRequestWhenDeletingWithInvalidId()
            throws Exception {

        mockMvc.perform(
                        delete("/api/v1/warehouses/0")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );

        verify(service, never())
                .delete(anyLong());
    }
}