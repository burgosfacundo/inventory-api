package com.burgosfacundo.inventory.supplier.controller;


import com.burgosfacundo.inventory.common.config.WebConfig;
import com.burgosfacundo.inventory.common.exception.GlobalExceptionHandler;
import com.burgosfacundo.inventory.supplier.dto.SupplierRequest;
import com.burgosfacundo.inventory.supplier.dto.SupplierResponse;
import com.burgosfacundo.inventory.supplier.exception.SupplierEmailAlreadyExistsException;
import com.burgosfacundo.inventory.supplier.exception.SupplierNotFoundException;
import com.burgosfacundo.inventory.supplier.service.SupplierService;
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

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
        controllers = SupplierController.class,
        properties = "api.base-path=/api/v1"
)
@Import({
        WebConfig.class,
        GlobalExceptionHandler.class
})
public class SupplierControllerTest {
    static Stream<Arguments> invalidFindAllParameters() {
        return Stream.of(
                Arguments.of("page", "-1"),
                Arguments.of("size", "0"),
                Arguments.of("size", "101")
        );
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private SupplierService service;

    private SupplierResponse response(){
        return  new SupplierResponse(1L,"name","email@supplier.com",
                "22","description",true);
    }

    private SupplierRequest request(){
        return new SupplierRequest("name","email@supplier.com",
                "22","description");
    }


    @Test
    public void shouldCreateSupplier() throws Exception {
        SupplierRequest request = request();

        SupplierResponse response = response();

        String json = objectMapper.writeValueAsString(request);

        when(service.save(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.email").value("email@supplier.com"))
                .andExpect(jsonPath("$.phone").value("22"))
                .andExpect(jsonPath("$.description").value("description"))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).save(request);
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        SupplierRequest request = new SupplierRequest("","email@supplier.com",
                "22","description");

        mockMvc.perform(
                        post("/api/v1/suppliers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never())
                .save(any(SupplierRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        SupplierRequest request = new SupplierRequest("Name","",
                "22","description");

        mockMvc.perform(
                        post("/api/v1/suppliers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never())
                .save(any(SupplierRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenEmailFormatIsInvalid() throws Exception {
        SupplierRequest request = new SupplierRequest(
                "Name",
                "invalid-email",
                "22",
                "description"
        );

        mockMvc.perform(
                        post("/api/v1/suppliers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never())
                .save(any(SupplierRequest.class));
    }


    @Test
    void shouldThrowConflictWhenEmailAlreadyExists() throws Exception {
        SupplierRequest request = request();

        when(service.save(request))
                .thenThrow(
                        new SupplierEmailAlreadyExistsException("email@suppler.com")
                );

        mockMvc.perform(
                        post("/api/v1/suppliers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("SUPPLIER_EMAIL_ALREADY_EXISTS"));

        verify(service)
                .save(request);
    }


    @Test
    void shouldFindSupplierById() throws Exception {
        SupplierResponse response = response();

        when(service.findById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/suppliers/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.email").value("email@supplier.com"))
                .andExpect(jsonPath("$.phone").value("22"))
                .andExpect(jsonPath("$.description").value("description"))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenSupplierDoesNotExist() throws Exception {
        when(service.findById(99L)).thenThrow(new SupplierNotFoundException(99L));

        mockMvc.perform(get("/api/v1/suppliers/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))                .andExpect(jsonPath("$.errorCode")
                        .value("SUPPLIER_NOT_FOUND"));

        verify(service).findById(99L);
    }

    @Test
    void shouldFindAllSuppliersWithDefaultParameters() throws Exception {
        SupplierResponse response = response();

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

        when(service.findAll(null, pageable))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/suppliers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))

                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].email").value("email@supplier.com"))
                .andExpect(jsonPath("$.content[0].name").value("name"))

                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(service).findAll(null, pageable);
    }


    @Test
    void shouldFindAllSuppliersWithFiltersPaginationAndSorting() throws Exception {
        SupplierResponse response = response();

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Direction.DESC, "email")
        );

        var page = new PageImpl<>(
                List.of(response),
                pageable,
                1
        );

        when(service.findAll(true, pageable))
                .thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/suppliers")
                                .param("page", "0")
                                .param("size", "10")
                                .param("active", "true")
                                .param("sortBy", "email")
                                .param("direction", "desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("email@supplier.com"))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).findAll(true, pageable);
    }


    @Test
    void shouldReturnBadRequestWhenSortFieldIsInvalid() throws Exception {
        mockMvc.perform(
                        get("/api/v1/suppliers")
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
                .findAll(any(), any(Pageable.class));
    }

    @Test
    void shouldReturnBadRequestWhenSortDirectionIsInvalid() throws Exception {
        mockMvc.perform(
                        get("/api/v1/suppliers")
                                .param("sortBy", "email")
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
                .findAll(any(), any(Pageable.class));
    }


    @ParameterizedTest
    @MethodSource("invalidFindAllParameters")
    void shouldReturnBadRequestWhenFindAllParametersAreInvalid(
            String parameter,
            String value
    ) throws Exception {

        mockMvc.perform(
                        get("/api/v1/suppliers")
                                .param(parameter, value)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never())
                .findAll(any(), any(Pageable.class));
    }


    @Test
    void shouldUpdateSupplier() throws Exception {
        SupplierRequest request = request();

        SupplierResponse response = response();

        String json = objectMapper.writeValueAsString(request);

        when(service.update(1L, request))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/suppliers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.email").value("email@supplier.com"))
                .andExpect(jsonPath("$.phone").value("22"))
                .andExpect(jsonPath("$.description").value("description"))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).update(1L, request);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingSupplier() throws Exception {
        SupplierRequest request = request();

        when(service.update(99L, request))
                .thenThrow(new SupplierNotFoundException(99L));

        mockMvc.perform(
                        put("/api/v1/suppliers/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("SUPPLIER_NOT_FOUND"));

        verify(service).update(99L, request);
    }


    @Test
    void shouldReturnConflictWhenUpdatingWithExistingEmail() throws Exception {
        SupplierRequest request = request();

        when(service.update(1L, request))
                .thenThrow(new SupplierEmailAlreadyExistsException("email@supplier.com"));

        mockMvc.perform(
                        put("/api/v1/suppliers/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("SUPPLIER_EMAIL_ALREADY_EXISTS"));

        verify(service).update(1L, request);
    }


    @Test
    void shouldReturnBadRequestWhenUpdateBodyIsInvalid() throws Exception {
        SupplierRequest request = new SupplierRequest("","","","");

        mockMvc.perform(
                        put("/api/v1/suppliers/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));

        verify(service, never())
                .update(anyLong(), any(SupplierRequest.class));
    }


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldUpdateSupplierStatus(boolean active) throws Exception {
        SupplierResponse response = new SupplierResponse(1L,"name","email",
                "phone","description",active);

        when(service.updateStatus(1L, active))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/suppliers/1/status")
                        .param("active", String.valueOf(active))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.active").value(active));


        verify(service).updateStatus(1L, active);
    }


    @Test
    void shouldReturnNotFoundWhenUpdatingStatusOfNonExistingSupplier()
            throws Exception {

        when(service.updateStatus(99L, false))
                .thenThrow(new SupplierNotFoundException(99L));

        mockMvc.perform(
                        patch("/api/v1/suppliers/99/status")
                                .param("active", "false")
                )
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("SUPPLIER_NOT_FOUND"));

        verify(service).updateStatus(99L, false);
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingStatusWithInvalidId()
            throws Exception {

        mockMvc.perform(
                        patch("/api/v1/suppliers/0/status")
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
                        patch("/api/v1/suppliers/1/status")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"));
    }


    @Test
    void shouldDeleteSupplier() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(
                        delete("/api/v1/suppliers/1")
                )
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingSupplier() throws Exception {
        doThrow(new SupplierNotFoundException(99L))
                .when(service)
                .delete(99L);

        mockMvc.perform(
                        delete("/api/v1/suppliers/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        ))
                .andExpect(jsonPath("$.errorCode")
                        .value("SUPPLIER_NOT_FOUND"));

        verify(service).delete(99L);
    }


    @Test
    void shouldReturnBadRequestWhenDeletingWithInvalidId() throws Exception {
        mockMvc.perform(
                        delete("/api/v1/suppliers/0")
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
