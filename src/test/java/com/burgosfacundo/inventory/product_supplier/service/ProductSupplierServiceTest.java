package com.burgosfacundo.inventory.product_supplier.service;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.product.exception.ProductNotFoundException;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product.repository.ProductRepository;
import com.burgosfacundo.inventory.product_supplier.dto.ProductSupplierRequest;
import com.burgosfacundo.inventory.product_supplier.exception.ProductSupplierAlreadyExistsException;
import com.burgosfacundo.inventory.product_supplier.exception.ProductSupplierNotFoundException;
import com.burgosfacundo.inventory.product_supplier.model.ProductSupplier;
import com.burgosfacundo.inventory.product_supplier.repository.ProductSupplierRepository;
import com.burgosfacundo.inventory.supplier.exception.SupplierNotFoundException;
import com.burgosfacundo.inventory.supplier.model.Supplier;
import com.burgosfacundo.inventory.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
public class ProductSupplierServiceTest {

    static Stream<Arguments> filters() {
        return Stream.of(
                Arguments.of(1L, null),
                Arguments.of(null, 2L),
                Arguments.of(1L, 2L)
        );
    }

    @Mock
    private ProductSupplierRepository repository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    private ProductSupplierService service;

    @BeforeEach
    void setUp() {
        service = new ProductSupplierServiceImpl(repository, productRepository, supplierRepository);
    }

    private Product productWithId() {
        Category category = new Category(
                "Category",
                "Description"
        );

        Product product = new Product(
                "SKU-1",
                "Product",
                "Description",
                new BigDecimal("100.00"),
                category
        );

        ReflectionTestUtils.setField(product, "id", 1L);

        return product;
    }

    private Supplier supplierWithId() {
        Supplier supplier = new Supplier(
                "Supplier",
                "supplier@email.com",
                "22",
                "Description"
        );

        ReflectionTestUtils.setField(supplier, "id", 2L);

        return supplier;
    }

    private ProductSupplier associationWithId(
            Product product,
            Supplier supplier
    ) {
        ProductSupplier association =
                new ProductSupplier(product, supplier);

        ReflectionTestUtils.setField(
                association,
                "id",
                10L
        );

        return association;
    }

    //Save ProductSupplier

    @Test
    void shouldCreateProductSupplierAssociation() {
        ProductSupplierRequest request =
                new ProductSupplierRequest(1L, 2L);

        Product product = productWithId();
        Supplier supplier = supplierWithId();

        ProductSupplier association =
                associationWithId(
                        product,
                        supplier
                );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(supplierRepository.findById(2L))
                .thenReturn(Optional.of(supplier));

        when(repository.existsByProductIdAndSupplierId(1L, 2L))
                .thenReturn(false);

        when(repository.save(any(ProductSupplier.class)))
                .thenReturn(association);

        var response = service.save(request);

        assertThat(response.id()).isEqualTo(10L);

        assertThat(response.product().id()).isEqualTo(1L);
        assertThat(response.product().sku()).isEqualTo("SKU-1");
        assertThat(response.product().name()).isEqualTo("Product");

        assertThat(response.supplier().id()).isEqualTo(2L);
        assertThat(response.supplier().name()).isEqualTo("Supplier");
        assertThat(response.supplier().email())
                .isEqualTo("supplier@email.com");

        verify(productRepository).findById(1L);
        verify(supplierRepository).findById(2L);

        verify(repository)
                .existsByProductIdAndSupplierId(1L, 2L);

        verify(repository)
                .save(any(ProductSupplier.class));
    }


    @Test
    void shouldThrowProductNotFoundWhenCreatingAssociation() {
        ProductSupplierRequest request =
                new ProductSupplierRequest(99L, 2L);

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                ProductNotFoundException.class,
                () -> service.save(request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo("PRODUCT_NOT_FOUND");

        verify(productRepository).findById(99L);

        verify(supplierRepository, never())
                .findById(anyLong());

        verify(repository, never())
                .existsByProductIdAndSupplierId(
                        anyLong(),
                        anyLong()
                );

        verify(repository, never())
                .save(any(ProductSupplier.class));
    }


    @Test
    void shouldThrowSupplierNotFoundWhenCreatingAssociation() {
        ProductSupplierRequest request =
                new ProductSupplierRequest(1L, 99L);

        Product product = productWithId();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(supplierRepository.findById(99L))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                SupplierNotFoundException.class,
                () -> service.save(request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo("SUPPLIER_NOT_FOUND");

        verify(productRepository).findById(1L);
        verify(supplierRepository).findById(99L);

        verify(repository, never())
                .existsByProductIdAndSupplierId(
                        anyLong(),
                        anyLong()
                );

        verify(repository, never())
                .save(any(ProductSupplier.class));
    }

    @Test
    void shouldThrowExceptionWhenAssociationAlreadyExists() {
        ProductSupplierRequest request =
                new ProductSupplierRequest(1L, 2L);

        Product product = productWithId();
        Supplier supplier = supplierWithId();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(supplierRepository.findById(2L))
                .thenReturn(Optional.of(supplier));

        when(repository.existsByProductIdAndSupplierId(1L, 2L))
                .thenReturn(true);

        var exception = assertThrows(
                ProductSupplierAlreadyExistsException.class,
                () -> service.save(request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo("PRODUCT_SUPPLIER_ALREADY_EXISTS");

        verify(repository)
                .existsByProductIdAndSupplierId(1L, 2L);

        verify(repository, never())
                .save(any(ProductSupplier.class));
    }


    //Find By Id
    @Test
    void shouldFindProductSupplierById() {
        Product product = productWithId();
        Supplier supplier = supplierWithId();

        ProductSupplier association =
                associationWithId(
                        product,
                        supplier
                );

        when(repository.findWithRelationsById(10L))
                .thenReturn(Optional.of(association));

        var response = service.findById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.product().id()).isEqualTo(1L);
        assertThat(response.supplier().id()).isEqualTo(2L);

        verify(repository)
                .findWithRelationsById(10L);
    }

    @Test
    void shouldThrowProductSupplierNotFoundWhenFindingById() {
        when(repository.findWithRelationsById(99L))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                ProductSupplierNotFoundException.class,
                () -> service.findById(99L)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo("PRODUCT_SUPPLIER_NOT_FOUND");

        verify(repository)
                .findWithRelationsById(99L);
    }

    //Find All
    @Test
    void shouldFindAllWithoutFilters() {
        Product product = productWithId();
        Supplier supplier = supplierWithId();

        ProductSupplier association =
                associationWithId(
                        product,
                        supplier
                );

        Pageable pageable = PageRequest.of(0, 20);

        Page<ProductSupplier> page =
                new PageImpl<>(
                        List.of(association),
                        pageable,
                        1
                );

        when(repository.findAllFiltered(
                null,
                null,
                pageable
        )).thenReturn(page);

        var result = service.findAll(
                null,
                null,
                pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().id())
                .isEqualTo(10L);

        verify(repository)
                .findAllFiltered(
                        null,
                        null,
                        pageable
                );
    }

    @ParameterizedTest
    @MethodSource("filters")
    void shouldFindAllWithFilters(
            Long productId,
            Long supplierId
    ) {
        Pageable pageable = PageRequest.of(0, 20);

        when(repository.findAllFiltered(
                productId,
                supplierId,
                pageable
        )).thenReturn(Page.empty(pageable));

        var result = service.findAll(
                productId,
                supplierId,
                pageable
        );

        assertThat(result).isEmpty();

        verify(repository)
                .findAllFiltered(
                        productId,
                        supplierId,
                        pageable
                );
    }


    //Delete
    @Test
    void shouldDeleteProductSupplierAssociation() {
        when(repository.existsById(10L))
                .thenReturn(true);

        service.delete(10L);

        verify(repository).existsById(10L);
        verify(repository).deleteById(10L);
    }

    @Test
    void shouldThrowProductSupplierNotFoundWhenDeleting() {
        when(repository.existsById(99L))
                .thenReturn(false);

        var exception = assertThrows(
                ProductSupplierNotFoundException.class,
                () -> service.delete(99L)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo("PRODUCT_SUPPLIER_NOT_FOUND");

        verify(repository).existsById(99L);

        verify(repository, never())
                .deleteById(99L);
    }
}
