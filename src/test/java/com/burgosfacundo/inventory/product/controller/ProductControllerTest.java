package com.burgosfacundo.inventory.product.controller;

import com.burgosfacundo.inventory.category.exception.CategoryNotFoundException;
import com.burgosfacundo.inventory.common.config.WebConfig;
import com.burgosfacundo.inventory.common.exception.GlobalExceptionHandler;
import com.burgosfacundo.inventory.product.dto.CategorySummaryResponse;
import com.burgosfacundo.inventory.product.dto.ProductRequest;
import com.burgosfacundo.inventory.product.dto.ProductResponse;
import com.burgosfacundo.inventory.product.exception.ProductNotFoundException;
import com.burgosfacundo.inventory.product.exception.ProductSkuAlreadyExistsException;
import com.burgosfacundo.inventory.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProductController.class,
        properties = "api.base-path=/api/v1"
)
@Import({
        WebConfig.class,
        GlobalExceptionHandler.class
})
public class ProductControllerTest {
    static Stream<Arguments> invalidFindAllParameters() {
        return Stream.of(
                Arguments.of("page", "-1"),
                Arguments.of("size", "0"),
                Arguments.of("size", "101"),
                Arguments.of("categoryId", "0")
        );
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private ProductService service;

    @Test
    void shouldCreateProduct() throws Exception {
        CategorySummaryResponse category = new CategorySummaryResponse(1L, "category");
        ProductResponse response = new ProductResponse(1L,"SKU","Name",
                "Description",new BigDecimal("99.99"),true,category);

        ProductRequest request = new ProductRequest("SKU","Name",
                "Description",new BigDecimal("99.99"),1L);

        String json = objectMapper.writeValueAsString(request);

        when(service.save(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU"))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.salePrice").value(99.99))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.category.name").value("category"));

        verify(service).save(request);
    }


    @Test
    void shouldReturnBadRequestWhenSkuIsBlank() throws Exception {
        ProductRequest request = new ProductRequest(
                "",
                "Product Name",
                "Product Description",
                new BigDecimal("99.99"),
                1L
        );

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never())
                .save(any(ProductRequest.class));
    }


    @Test
    void shouldReturnConflictWhenSkuAlreadyExists() throws Exception {
        ProductRequest request = new ProductRequest(
                "SKU123",
                "Product Name",
                "Product Description",
                new BigDecimal("99.99"),
                1L
        );

        when(service.save(request))
                .thenThrow(
                        new ProductSkuAlreadyExistsException("SKU123")
                );

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("PRODUCT_SKU_ALREADY_EXISTS"));

        verify(service).save(request);
    }

    @Test
    void shouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
        ProductRequest request = new ProductRequest(
                "SKU123",
                "Product Name",
                "Product Description",
                new BigDecimal("99.99"),
                99L
        );

        when(service.save(request))
                .thenThrow(
                        new CategoryNotFoundException(99L)
                );

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("CATEGORY_NOT_FOUND"));

        verify(service).save(request);
    }



    @Test
    void shouldFindProductById() throws Exception {
        CategorySummaryResponse category = new CategorySummaryResponse(1L, "category");
        ProductResponse response = new ProductResponse(1L,"SKU","Name",
                "Description",new BigDecimal("99.99"),true,category);

        when(service.findById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU"))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.salePrice").value(99.99))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.category.name").value("category"));


        verify(service).findById(1L);
    }


    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(service.findById(99L))
                .thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail")
                        .value("Product not found with id: 99"))
                .andExpect(jsonPath("$.instance")
                        .value("/api/v1/products/99"))
                .andExpect(jsonPath("$.errorCode")
                        .value("PRODUCT_NOT_FOUND"));

        verify(service).findById(99L);
    }


    @Test
    void shouldFindAllProductsWithDefaultParameters() throws Exception {
        CategorySummaryResponse category =
                new CategorySummaryResponse(1L, "Category");

        ProductResponse product1 = new ProductResponse(
                1L,
                "SKU1",
                "Product 1",
                "Description 1",
                new BigDecimal("10.00"),
                true,
                category
        );

        ProductResponse product2 = new ProductResponse(
                2L,
                "SKU2",
                "Product 2",
                "Description 2",
                new BigDecimal("20.00"),
                false,
                category
        );

        Pageable pageable = PageRequest.of(
                0,
                20,
                Sort.by(Sort.Direction.ASC, "id")
        );

        var page = new PageImpl<>(
                List.of(product1, product2),
                pageable,
                2
        );

        when(service.findAll(null, null, pageable))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))

                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("SKU1"))
                .andExpect(jsonPath("$.content[0].category.id").value(1))

                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].sku").value("SKU2"))

                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(service).findAll(null, null, pageable);
    }


    @Test
    void shouldFindAllProductsWithFiltersPaginationAndSorting() throws Exception {
        CategorySummaryResponse category =
                new CategorySummaryResponse(1L, "Category");

        ProductResponse product = new ProductResponse(
                1L,
                "SKU1",
                "Product 1",
                "Description",
                new BigDecimal("99.99"),
                true,
                category
        );

        Pageable pageable = PageRequest.of(
                1,
                10,
                Sort.by(Sort.Direction.DESC, "salePrice")
        );

        var page = new PageImpl<>(
                List.of(product),
                pageable,
                11
        );

        when(service.findAll(1L, true, pageable))
                .thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/products")
                                .param("page", "1")
                                .param("size", "10")
                                .param("categoryId", "1")
                                .param("active", "true")
                                .param("sortBy", "salePrice")
                                .param("direction", "desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("SKU1"))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(11));

        verify(service).findAll(1L, true, pageable);
    }


    @Test
    void shouldReturnBadRequestWhenSortFieldIsInvalid() throws Exception {
        mockMvc.perform(
                        get("/api/v1/products")
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
                .findAll(any(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnBadRequestWhenSortDirectionIsInvalid() throws Exception {
        mockMvc.perform(
                        get("/api/v1/products")
                                .param("sortBy", "name")
                                .param("direction", "invalid")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("INVALID_SORT"))
                .andExpect(jsonPath("$.detail")
                        .value("Invalid sort direction: invalid"));

        verify(service, never())
                .findAll(any(), any(), any(Pageable.class));
    }


    @ParameterizedTest
    @MethodSource("invalidFindAllParameters")
    void shouldReturnBadRequestWhenFindAllParametersAreInvalid(
            String parameter,
            String value
    ) throws Exception {

        mockMvc.perform(
                        get("/api/v1/products")
                                .param(parameter, value)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never())
                .findAll(any(), any(), any(Pageable.class));
    }



    @Test
    void shouldUpdateProduct() throws Exception {
        ProductRequest request = new ProductRequest("SKU","Name",
                "Description",new BigDecimal("99.99"),1L);

        CategorySummaryResponse category = new CategorySummaryResponse(1L, "category");
        ProductResponse response = new ProductResponse(1L,"SKU","Name",
                "Description",new BigDecimal("99.99"),true,category);

        String json = objectMapper.writeValueAsString(request);

        when(service.update(1L, request))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU"))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.salePrice").value(99.99))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.category.name").value("category"));


        verify(service).update(1L, request);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingProduct() throws Exception {
        ProductRequest request = new ProductRequest(
                "SKU",
                "Name",
                "Description",
                new BigDecimal("99.99"),
                1L
        );

        when(service.update(99L, request))
                .thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(
                        put("/api/v1/products/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("PRODUCT_NOT_FOUND"));

        verify(service).update(99L, request);
    }


    @Test
    void shouldReturnConflictWhenUpdatingWithExistingSku() throws Exception {
        ProductRequest request = new ProductRequest(
                "SKU",
                "Name",
                "Description",
                new BigDecimal("99.99"),
                1L
        );

        when(service.update(1L, request))
                .thenThrow(new ProductSkuAlreadyExistsException("SKU"));

        mockMvc.perform(
                        put("/api/v1/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("PRODUCT_SKU_ALREADY_EXISTS"));

        verify(service).update(1L, request);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingCategory() throws Exception {
        ProductRequest request = new ProductRequest(
                "SKU",
                "Name",
                "Description",
                new BigDecimal("99.99"),
                99L
        );

        when(service.update(1L, request))
                .thenThrow(new CategoryNotFoundException(99L));

        mockMvc.perform(
                        put("/api/v1/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("CATEGORY_NOT_FOUND"));

        verify(service).update(1L, request);
    }

    @Test
    void shouldReturnBadRequestWhenUpdateBodyIsInvalid() throws Exception {
        ProductRequest request = new ProductRequest(
                "",
                "Name",
                "Description",
                new BigDecimal("99.99"),
                1L
        );

        mockMvc.perform(
                        put("/api/v1/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never())
                .update(anyLong(), any(ProductRequest.class));
    }



    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldUpdateProductStatus(boolean active) throws Exception {

        CategorySummaryResponse category = new CategorySummaryResponse(1L, "category");
        ProductResponse response = new ProductResponse(1L,"SKU","Name",
                "Description",new BigDecimal("99.99"),active,category);

        when(service.updateStatus(1L, active))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/products/1/status")
                        .param("active", String.valueOf(active))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.active").value(active));


        verify(service).updateStatus(1L, active);
    }


    @Test
    void shouldReturnNotFoundWhenUpdatingStatusOfNonExistingProduct()
            throws Exception {

        when(service.updateStatus(99L, false))
                .thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(
                        patch("/api/v1/products/99/status")
                                .param("active", "false")
                )
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("PRODUCT_NOT_FOUND"));

        verify(service).updateStatus(99L, false);
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingStatusWithInvalidId()
            throws Exception {

        mockMvc.perform(
                        patch("/api/v1/products/0/status")
                                .param("active", "true")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never())
                .updateStatus(anyLong(), anyBoolean());
    }

    @Test
    void shouldReturnBadRequestWhenActiveParameterIsMissing()
            throws Exception {

        mockMvc.perform(
                        patch("/api/v1/products/1/status")
                )
                .andExpect(status().isBadRequest());

        verify(service, never())
                .updateStatus(anyLong(), anyBoolean());
    }


    @Test
    void shouldDeleteProduct() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(
                        delete("/api/v1/products/1")
                )
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingProduct() throws Exception {
        doThrow(new ProductNotFoundException(99L))
                .when(service)
                .delete(99L);

        mockMvc.perform(
                        delete("/api/v1/products/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.detail")
                        .value("Product not found with id: 99"));

        verify(service).delete(99L);
    }


    @Test
    void shouldReturnBadRequestWhenDeletingWithInvalidId() throws Exception {
        mockMvc.perform(
                        delete("/api/v1/products/0")
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
