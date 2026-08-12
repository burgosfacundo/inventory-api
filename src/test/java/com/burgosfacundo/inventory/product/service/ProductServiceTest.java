package com.burgosfacundo.inventory.product.service;

import com.burgosfacundo.inventory.category.exception.CategoryNotFoundException;
import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.category.repository.CategoryRepository;
import com.burgosfacundo.inventory.product.dto.ProductRequest;
import com.burgosfacundo.inventory.product.exception.ProductNotFoundException;
import com.burgosfacundo.inventory.product.exception.ProductSkuAlreadyExistsException;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(repository, categoryRepository);
    }

    private Category categoryWithId(
            String name,
            String description
    ) {
        Category category = new Category(name, description);
        ReflectionTestUtils.setField(category, "id", 1L);
        return category;
    }

    private Product productWithId(
            Long id,
            String sku,
            String name,
            String description,
            BigDecimal salePrice,
            Category category
    ) {
        Product product = new Product(
                sku,
                name,
                description,
                salePrice,
                category
        );

        ReflectionTestUtils.setField(product, "id", id);

        return product;
    }


    //Save Product
    @Test
    void shouldCreateProduct() {
        ProductRequest request = new ProductRequest(
                "SKU123",
                "Product Name",
                "Product Description",
                new BigDecimal("99.99"),
                1L
        );

        Category category = categoryWithId(
                "Category",
                "Description"
        );

        Product savedProduct = productWithId(
                1L,
                request.sku(),
                request.name(),
                request.description(),
                request.salePrice(),
                category
        );

        when(repository.existsBySku(request.sku()))
                .thenReturn(false);

        when(categoryRepository.findById(request.idCategory()))
                .thenReturn(Optional.of(category));

        when(repository.save(any(Product.class)))
                .thenReturn(savedProduct);

        var response = service.save(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.sku()).isEqualTo("SKU123");
        assertThat(response.name()).isEqualTo("Product Name");
        assertThat(response.description())
                .isEqualTo("Product Description");
        assertThat(response.salePrice())
                .isEqualByComparingTo("99.99");
        assertThat(response.active()).isTrue();

        assertThat(response.category().id()).isEqualTo(1L);
        assertThat(response.category().name())
                .isEqualTo("Category");

        verify(repository).existsBySku("SKU123");
        verify(categoryRepository).findById(1L);
        verify(repository).save(any(Product.class));
    }


    @Test
    void shouldThrowExceptionWhenSkuIsRepeated(){
        ProductRequest request = new ProductRequest(
                "SKU123",
                "Product Name",
                "Product Description",
                new BigDecimal("99.99"),
                1L
        );

        when(repository.existsBySku(request.sku()))
                .thenReturn(true);

        assertThrows(ProductSkuAlreadyExistsException.class, () -> service.save(request));

        verify(repository).existsBySku("SKU123");
        verify(categoryRepository, never()).findById(anyLong());
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound(){
        ProductRequest request = new ProductRequest(
                "SKU123",
                "Product Name",
                "Product Description",
                new BigDecimal("99.99"),
                1L
        );


        when(repository.existsBySku(request.sku()))
                .thenReturn(false);

        when(categoryRepository.findById(request.idCategory()))
                .thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> service.save(request));

        verify(repository).existsBySku("SKU123");
        verify(categoryRepository).findById(1L);
        verify(repository, never()).save(any(Product.class));
    }

    //Find By Id
    @Test
    void shouldFindById(){
        Product product = productWithId(1L,
                "SKU123",
                "Product Name",
                "Product Description",
                new BigDecimal("99.99"),
                categoryWithId("Category", "Description"));

        when(repository.findWithCategoryById(1L))
                .thenReturn(Optional.of(product));

        var result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.sku()).isEqualTo("SKU123");
        assertThat(result.name()).isEqualTo("Product Name");
        assertThat(result.description()).isEqualTo("Product Description");
        assertThat(result.salePrice()).isEqualTo(new BigDecimal("99.99"));

        verify(repository).findWithCategoryById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound(){
        when(repository.findWithCategoryById(1L))
                .thenReturn(Optional.empty());

        var exception = assertThrows(ProductNotFoundException.class, () -> service.findById(1L));
        assertThat(exception.getMessage()).isEqualTo("Product not found with id: 1");
        assertThat(exception.getErrorCode()).isEqualTo("PRODUCT_NOT_FOUND");

        verify(repository).findWithCategoryById(1L);
    }

    //Find All
    @Test
    void shouldFindAllProductsWithoutFilters() {
        Category category = categoryWithId(
                "Category",
                "Description"
        );

        Product product1 = productWithId(
                1L,
                "SKU1",
                "Product 1",
                "Description 1",
                new BigDecimal("10.00"),
                category
        );

        Product product2 = productWithId(
                2L,
                "SKU2",
                "Product 2",
                "Description 2",
                new BigDecimal("20.00"),
                category
        );

        Pageable pageable = PageRequest.of(0, 20);

        Page<Product> productPage = new PageImpl<>(
                List.of(product1, product2),
                pageable,
                2
        );

        when(repository.findAllFiltered(null, null, pageable))
                .thenReturn(productPage);

        var result = service.findAll(null, null, pageable);

        assertThat(result.getContent()).hasSize(2);

        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getContent().get(0).sku()).isEqualTo("SKU1");

        assertThat(result.getContent().get(1).id()).isEqualTo(2L);
        assertThat(result.getContent().get(1).sku()).isEqualTo("SKU2");

        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(repository).findAllFiltered(null, null, pageable);
    }

    @ParameterizedTest(name = "categoryId={0}, active={1}")
    @MethodSource("productFilterCases")
    void shouldDelegateFiltersToRepository(
            Long categoryId,
            Boolean active
    ) {
        Pageable pageable = PageRequest.of(0, 20);

        when(repository.findAllFiltered(categoryId, active, pageable))
                .thenReturn(Page.empty(pageable));

        var result = service.findAll(
                categoryId,
                active,
                pageable
        );

        assertThat(result.getContent()).isEmpty();

        verify(repository)
                .findAllFiltered(categoryId, active, pageable);
    }

    //Update Product

    @Test
    void shouldUpdateProduct(){
        Category category = categoryWithId(
                "Category",
                "Description"
        );


        Product existingProduct = productWithId(
                1L,
                "OLD-SKU",
                "Old Name",
                "Old Description",
                new BigDecimal("50.00"),
                category
        );

        ProductRequest request = new ProductRequest(
                "SKU1234",
                "Name",
                "Description",
                new BigDecimal("100.00"),
                1L
        );

        when(repository.existsBySkuAndIdNot(request.sku(),1L))
                .thenReturn(false);

        when(repository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        var result = service.update(1L, request);

        assertThat(result.sku()).isEqualTo("SKU1234");
        assertThat(result.name()).isEqualTo("Name");
        assertThat(result.description()).isEqualTo("Description");
        assertThat(result.salePrice()).isEqualByComparingTo("100.00");

        verify(repository).findById(1L);
        verify(repository).existsBySkuAndIdNot("SKU1234", 1L);
        verify(categoryRepository).findById(1L);
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenSkuAlreadyExists() {
        Category category = categoryWithId(
                "Category",
                "Description"
        );

        Product existingProduct = productWithId(
                1L,
                "OLD-SKU",
                "Old Name",
                "Old Description",
                new BigDecimal("50.00"),
                category
        );

        ProductRequest request = new ProductRequest(
                "SKU1234",
                "Name",
                "Description",
                new BigDecimal("100.00"),
                1L
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(repository.existsBySkuAndIdNot("SKU1234", 1L))
                .thenReturn(true);

        assertThrows(
                ProductSkuAlreadyExistsException.class,
                () -> service.update(1L, request)
        );

        verify(repository).findById(1L);
        verify(repository).existsBySkuAndIdNot("SKU1234", 1L);
        verify(categoryRepository, never()).findById(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundForUpdate() {
        ProductRequest request = new ProductRequest(
                "SKU1234",
                "Name",
                "Description",
                new BigDecimal("100.00"),
                1L
        );

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> service.update(1L, request)
        );

        verify(repository).findById(1L);
        verify(repository, never())
                .existsBySkuAndIdNot(anyString(), anyLong());
        verify(categoryRepository, never()).findById(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFoundForUpdate() {
        Category category = categoryWithId(
                "Category",
                "Description"
        );

        Product existingProduct = productWithId(
                1L,
                "OLD-SKU",
                "Old Name",
                "Old Description",
                new BigDecimal("50.00"),
                category
        );

        ProductRequest request = new ProductRequest(
                "SKU1234",
                "Name",
                "Description",
                new BigDecimal("100.00"),
                1L
        );
        when(repository.existsBySkuAndIdNot(request.sku(), 1L))
                .thenReturn(false);

        when(repository.findById(1L)).thenReturn(Optional.of(existingProduct));

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> service.update(1L, request));
        verify(repository).existsBySkuAndIdNot(request.sku(), 1L);
        verify(repository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(repository, never()).save(any(Product.class));
    }

    //Update status
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldUpdateStatus(boolean active) {
        Category category = categoryWithId(
                "name category",
                null
        );

        Product existingProduct = productWithId(
                1L,
                "sku",
                "name",
                "description",
                new BigDecimal("99.99"),
                category
        );

        // Initial state contrary to the one requested
        if (active) {
            existingProduct.deactivate();
        } else {
            existingProduct.activate();
        }

        when(repository.findWithCategoryById(1L))
                .thenReturn(Optional.of(existingProduct));

        var result = service.updateStatus(1L, active);

        assertThat(result.active()).isEqualTo(active);
        assertThat(existingProduct.getActive()).isEqualTo(active);

        verify(repository).findWithCategoryById(1L);
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundForUpdateStatus() {
        when(repository.findWithCategoryById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.updateStatus(1L, false));
        verify(repository).findWithCategoryById(1L);
        verify(repository, never()).save(any(Product.class));
    }

    //Delete Product
    @Test
    void shouldDeleteProduct(){
        when(repository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundForDelete() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> service.delete(1L));
        verify(repository).existsById(1L);
        verify(repository, never()).deleteById(1L);
    }




    static Stream<Arguments> productFilterCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(1L, null),
                Arguments.of(null, true),
                Arguments.of(null, false),
                Arguments.of(1L, true),
                Arguments.of(1L, false)
        );
    }
}