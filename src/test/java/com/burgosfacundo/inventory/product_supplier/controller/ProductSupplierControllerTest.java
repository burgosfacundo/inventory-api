package com.burgosfacundo.inventory.product_supplier.controller;

import com.burgosfacundo.inventory.common.config.WebConfig;
import com.burgosfacundo.inventory.common.exception.GlobalExceptionHandler;
import com.burgosfacundo.inventory.product.exception.ProductNotFoundException;
import com.burgosfacundo.inventory.product_supplier.dto.ProductSummaryResponse;
import com.burgosfacundo.inventory.product_supplier.dto.ProductSupplierRequest;
import com.burgosfacundo.inventory.product_supplier.dto.ProductSupplierResponse;
import com.burgosfacundo.inventory.product_supplier.dto.SupplierSummaryResponse;
import com.burgosfacundo.inventory.product_supplier.exception.ProductSupplierAlreadyExistsException;
import com.burgosfacundo.inventory.product_supplier.exception.ProductSupplierNotFoundException;
import com.burgosfacundo.inventory.product_supplier.service.ProductSupplierService;
import com.burgosfacundo.inventory.supplier.exception.SupplierNotFoundException;
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
        controllers = ProductSupplierController.class,
        properties = "api.base-path=/api/v1"
)
@Import({
        WebConfig.class,
        GlobalExceptionHandler.class
})
class ProductSupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductSupplierService service;

    private ProductSupplierRequest request() {
        return new ProductSupplierRequest(1L, 2L);
    }

    private ProductSupplierResponse response() {
        ProductSummaryResponse product =
                new ProductSummaryResponse(
                        1L,
                        "SKU-1",
                        "Product"
                );

        SupplierSummaryResponse supplier =
                new SupplierSummaryResponse(
                        2L,
                        "Supplier",
                        "supplier@email.com"
                );

        return new ProductSupplierResponse(
                10L,
                product,
                supplier
        );
    }

    static Stream<Arguments> invalidFindAllParameters() {
        return Stream.of(
                Arguments.of("page", "-1"),
                Arguments.of("size", "0"),
                Arguments.of("size", "101"),
                Arguments.of("productId", "0"),
                Arguments.of("supplierId", "0")
        );
    }


    @Test
    void shouldCreateProductSupplierAssociation() throws Exception {
        ProductSupplierRequest request = request();
        ProductSupplierResponse response = response();

        when(service.save(request))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/product-suppliers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.id").value(10))

                .andExpect(jsonPath("$.product.id").value(1))
                .andExpect(jsonPath("$.product.sku").value("SKU-1"))
                .andExpect(jsonPath("$.product.name").value("Product"))

                .andExpect(jsonPath("$.supplier.id").value(2))
                .andExpect(jsonPath("$.supplier.name").value("Supplier"))
                .andExpect(jsonPath("$.supplier.email")
                        .value("supplier@email.com"));

        verify(service).save(request);
    }


    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        ProductSupplierRequest request =
                new ProductSupplierRequest(0L, 2L);

        mockMvc.perform(
                        post("/api/v1/product-suppliers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never())
                .save(any(ProductSupplierRequest.class));
    }


    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        ProductSupplierRequest request = request();

        when(service.save(request))
                .thenThrow(new ProductNotFoundException(1L));

        mockMvc.perform(
                        post("/api/v1/product-suppliers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("PRODUCT_NOT_FOUND"));

        verify(service).save(request);
    }


    @Test
    void shouldReturnNotFoundWhenSupplierDoesNotExist() throws Exception {
        ProductSupplierRequest request = request();

        when(service.save(request))
                .thenThrow(new SupplierNotFoundException(2L));

        mockMvc.perform(
                        post("/api/v1/product-suppliers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("SUPPLIER_NOT_FOUND"));

        verify(service).save(request);
    }


    @Test
    void shouldReturnConflictWhenAssociationAlreadyExists()
            throws Exception {

        ProductSupplierRequest request = request();

        when(service.save(request))
                .thenThrow(
                        new ProductSupplierAlreadyExistsException(
                                1L,
                                2L
                        )
                );

        mockMvc.perform(
                        post("/api/v1/product-suppliers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("PRODUCT_SUPPLIER_ALREADY_EXISTS"));

        verify(service).save(request);
    }


    @Test
    void shouldFindProductSupplierById() throws Exception {
        ProductSupplierResponse response = response();

        when(service.findById(10L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/product-suppliers/10")
                )
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.product.id").value(1))
                .andExpect(jsonPath("$.supplier.id").value(2));

        verify(service).findById(10L);
    }


    @Test
    void shouldReturnNotFoundWhenAssociationDoesNotExist()
            throws Exception {

        when(service.findById(99L))
                .thenThrow(
                        new ProductSupplierNotFoundException(99L)
                );

        mockMvc.perform(
                        get("/api/v1/product-suppliers/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("PRODUCT_SUPPLIER_NOT_FOUND"));

        verify(service).findById(99L);
    }


    @Test
    void shouldReturnBadRequestWhenFindingWithInvalidId()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/product-suppliers/0")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never()).findById(anyLong());
    }


    @Test
    void shouldFindAllWithDefaultParameters() throws Exception {
        ProductSupplierResponse response = response();

        Pageable pageable = PageRequest.of(
                0,
                20,
                Sort.by(Sort.Direction.ASC, "id")
        );

        var page = new PageImpl<>(
                List.of(response),
                pageable,
                1
        );

        when(service.findAll(
                null,
                null,
                pageable
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/product-suppliers")
                )
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))

                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].product.id").value(1))
                .andExpect(jsonPath("$.content[0].supplier.id").value(2))

                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).findAll(
                null,
                null,
                pageable
        );
    }


    @Test
    void shouldFindAllWithFiltersPaginationAndSorting()
            throws Exception {

        ProductSupplierResponse response = response();

        Pageable pageable = PageRequest.of(
                1,
                10,
                Sort.by(
                        Sort.Direction.DESC,
                        "supplier.id"
                )
        );

        var page = new PageImpl<>(
                List.of(response),
                pageable,
                11
        );

        when(service.findAll(
                1L,
                2L,
                pageable
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/product-suppliers")
                                .param("page", "1")
                                .param("size", "10")
                                .param("productId", "1")
                                .param("supplierId", "2")
                                .param("sortBy", "supplier.id")
                                .param("direction", "desc")
                )
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(10))

                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(11));

        verify(service).findAll(
                1L,
                2L,
                pageable
        );
    }


    @Test
    void shouldReturnBadRequestWhenSortFieldIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/product-suppliers")
                                .param("sortBy", "invalidField")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("INVALID_SORT"))
                .andExpect(jsonPath("$.detail")
                        .value("Invalid sort field: invalidField"));

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
                        get("/api/v1/product-suppliers")
                                .param("sortBy", "id")
                                .param("direction", "invalid")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("INVALID_SORT"));

        verify(service, never())
                .findAll(
                        any(),
                        any(),
                        any(Pageable.class)
                );
    }


    @ParameterizedTest
    @MethodSource("invalidFindAllParameters")
    void shouldReturnBadRequestWhenFindAllParametersAreInvalid(
            String parameter,
            String value
    ) throws Exception {

        mockMvc.perform(
                        get("/api/v1/product-suppliers")
                                .param(parameter, value)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never())
                .findAll(
                        any(),
                        any(),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldDeleteProductSupplierAssociation()
            throws Exception {

        mockMvc.perform(
                        delete("/api/v1/product-suppliers/10")
                )
                .andExpect(status().isNoContent());

        verify(service).delete(10L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingAssociation()
            throws Exception {

        doThrow(
                new ProductSupplierNotFoundException(99L)
        )
                .when(service)
                .delete(99L);

        mockMvc.perform(
                        delete("/api/v1/product-suppliers/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("PRODUCT_SUPPLIER_NOT_FOUND"));

        verify(service).delete(99L);
    }

    @Test
    void shouldReturnBadRequestWhenDeletingWithInvalidId()
            throws Exception {

        mockMvc.perform(
                        delete("/api/v1/product-suppliers/0")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never()).delete(anyLong());
    }
}