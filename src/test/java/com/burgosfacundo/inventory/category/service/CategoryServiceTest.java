package com.burgosfacundo.inventory.category.service;

import com.burgosfacundo.inventory.category.dto.CategoryRequest;
import com.burgosfacundo.inventory.category.dto.CategoryResponse;
import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.category.exception.CategoryNotFoundException;
import com.burgosfacundo.inventory.category.exception.NameRequiredException;
import com.burgosfacundo.inventory.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    private CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryServiceImpl(repository);
    }

    private Category categoryWithId(
            Long id,
            String name,
            String description
    ) {
        Category category = new Category(name, description);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    @Test
    void shouldCreateCategory() {
        CategoryRequest request =
                new CategoryRequest("Electronics", "Electronic devices");

        when(repository.save(any(Category.class)))
                .thenReturn(categoryWithId(1L,request.name(), request.description()));

        var response = service.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Electronics");
        assertThat(response.description()).isEqualTo("Electronic devices");

        verify(repository).save(any(Category.class));
    }


    @Test
    void shouldNotPersistCategoryWhenNameIsInvalid() {
        CategoryRequest request =
                new CategoryRequest(null, "Description");

        assertThrows(NameRequiredException.class,() ->service.create(request));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldFindCategoryById() {
        Category category = categoryWithId(1L,"Electronics", "Devices");

        when(repository.findById(1L))
                .thenReturn(Optional.of(category));

        var result = service.findById(1L);

        assertThat(result.name()).isEqualTo("Electronics");
        assertThat(result.description()).isEqualTo("Devices");

        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                CategoryNotFoundException.class,
                () -> service.findById(1L)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Category not found with id: 1");

        verify(repository).findById(1L);
    }

    @Test
    void shouldFindAllCategories() {
        Category category = categoryWithId(1L,"Electronics", "Devices");
        Category category2 = categoryWithId(2L,"Technology", "Devices");

        when(repository.findAll()).thenReturn(List.of(category,category2));
        var result = service.findAll();

        CategoryResponse expected =
                new CategoryResponse(1L, "Electronics", "Devices");
        CategoryResponse expected2 =
                new CategoryResponse(2L, "Technology", "Devices");

        assertThat(result).containsExactly(expected,expected2);

        verify(repository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoCategoriesFound() {
        when(repository.findAll()).thenReturn(List.of());
        var result = service.findAll();

        assertThat(result).isEmpty();

        verify(repository).findAll();
    }

    @Test
    void shouldUpdateCategory() {
        Category category = categoryWithId(1L,"Electronics", "Old description");
        CategoryRequest request =
                new CategoryRequest("Technology", "New description");

        when(repository.findById(1L))
                .thenReturn(Optional.of(category));

        var result = service.update(1L, request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Technology");
        assertThat(result.description()).isEqualTo("New description");

        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingCategoryNotFound() {
        CategoryRequest request =
                new CategoryRequest("Technology", "New description");

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> service.update(1L, request)
        );

        verify(repository).findById(1L);
    }
}
