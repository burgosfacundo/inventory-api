package com.burgosfacundo.inventory.category.controller;

import com.burgosfacundo.inventory.category.dto.CategoryRequest;
import com.burgosfacundo.inventory.category.dto.CategoryResponse;
import com.burgosfacundo.inventory.category.exception.CategoryNotFoundException;
import com.burgosfacundo.inventory.category.service.CategoryService;
import com.burgosfacundo.inventory.common.config.WebConfig;
import com.burgosfacundo.inventory.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CategoryController.class,
        properties = "api.base-path=/api/v1"
)
@Import({
        WebConfig.class,
        GlobalExceptionHandler.class
})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldCreateCategory() throws Exception {
        CategoryResponse response =
                new CategoryResponse(1L, "Electronics", "Electronic devices");

        CategoryRequest request =
                new CategoryRequest("Electronics", "Electronic devices");

        String json = objectMapper.writeValueAsString(request);

        when(categoryService.create(any(CategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Electronic devices"));

        verify(categoryService).create(any());
    }


    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        CategoryRequest request =
                new CategoryRequest("", "Electronic devices");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").value("Name is required"));

        verify(categoryService, never()).create(any());
    }


    @Test
    void shouldFindCategoryById() throws Exception {
        when(categoryService.findById(1L))
                .thenReturn(
                        new CategoryResponse(
                                1L,
                                "Electronics",
                                "Devices"
                        )
                );

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Devices"));

        verify(categoryService).findById(1L);
    }


    @Test
    void shouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
        when(categoryService.findById(99L))
                .thenThrow(new CategoryNotFoundException(99L));

        mockMvc.perform(get("/api/v1/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail")
                        .value("Category not found with id: 99"))
                .andExpect(jsonPath("$.instance")
                        .value("/api/v1/categories/99"))
                .andExpect(jsonPath("$.errorCode")
                        .value("CATEGORY_NOT_FOUND"));

        verify(categoryService).findById(99L);
    }


    @Test
    void shouldFindAllCategories() throws Exception {
        when(categoryService.findAll())
                .thenReturn(List.of(
                        new CategoryResponse(1L, "Electronics", "Devices"),
                        new CategoryResponse(2L, "Furniture", "Home furniture")
                ));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Furniture"));

        verify(categoryService).findAll();
    }


    @Test
    void shouldUpdateCategory() throws Exception {
        CategoryRequest request =
                new CategoryRequest("Technology", "Updated description");

        String json = objectMapper.writeValueAsString(request);

        when(categoryService.update(eq(1L), any(CategoryRequest.class)))
                .thenReturn(
                        new CategoryResponse(
                                1L,
                                "Technology",
                                "Updated description"
                        )
                );

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Technology"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"));

        verify(categoryService).update(eq(1L), any());
    }


    @Test
    void shouldReturnBadRequestWhenIdIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/v1/categories/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(categoryService, never()).findById(anyLong());
    }
}