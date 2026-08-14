package com.burgosfacundo.inventory.product_supplier.repository;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.category.repository.CategoryRepository;
import com.burgosfacundo.inventory.config.IntegrationTest;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product.repository.ProductRepository;
import com.burgosfacundo.inventory.product_supplier.model.ProductSupplier;
import com.burgosfacundo.inventory.supplier.model.Supplier;
import com.burgosfacundo.inventory.supplier.repository.SupplierRepository;
import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
class ProductSupplierRepositoryIT extends IntegrationTest {

    @Autowired
    private ProductSupplierRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Category createCategory() {
        return categoryRepository.saveAndFlush(
                new Category("Category", null)
        );
    }

    private Product createProduct(
            String sku,
            Category category
    ) {
        return productRepository.saveAndFlush(
                new Product(
                        sku,
                        "Product",
                        null,
                        new BigDecimal("100.00"),
                        category
                )
        );
    }

    private Supplier createSupplier(String email) {
        return supplierRepository.saveAndFlush(
                new Supplier(
                        "Supplier",
                        email,
                        null,
                        null
                )
        );
    }

    private ProductSupplier createAssociation(Product product, Supplier supplier) {
        return repository.saveAndFlush(
                new ProductSupplier(product, supplier, new BigDecimal("80.00"))
        );
    }

    @Test
    void shouldPersistAndRetrieveProductSupplier() {
        Category category = createCategory();
        Product product = createProduct("SKU-1", category);
        Supplier supplier = createSupplier("supplier1@email.com");

        ProductSupplier relation = createAssociation(product, supplier);

        entityManager.clear();

        ProductSupplier found = repository
                .findWithRelationsById(relation.getId())
                .orElseThrow();

        assertThat(found.getId()).isEqualTo(relation.getId());
        assertThat(found.getProduct().getId()).isEqualTo(product.getId());
        assertThat(found.getSupplier().getId()).isEqualTo(supplier.getId());
    }

    @Test
    void shouldDetectExistingProductSupplierAssociation() {
        Category category = createCategory();
        Product product = createProduct("SKU-1", category);
        Supplier supplier = createSupplier("supplier1@email.com");

        createAssociation(product, supplier);

        assertThat(
                repository.existsByProductIdAndSupplierId(
                        product.getId(),
                        supplier.getId()
                )
        ).isTrue();

        assertThat(
                repository.existsByProductIdAndSupplierId(
                        product.getId(),
                        999L
                )
        ).isFalse();
    }


    @Test
    void shouldRejectDuplicateProductSupplierAssociation() {
        Category category = createCategory();
        Product product = createProduct("SKU-1", category);
        Supplier supplier = createSupplier("supplier1@email.com");

        createAssociation(product, supplier);

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new ProductSupplier(product, supplier, new BigDecimal("80.00"))));
    }



    @Test
    void shouldFilterProductSupplierAssociations() {
        Category category = createCategory();

        Product productA = createProduct("SKU-A", category);
        Product productB = createProduct("SKU-B", category);

        Supplier supplier1 = createSupplier("supplier1@email.com");
        Supplier supplier2 = createSupplier("supplier2@email.com");

        ProductSupplier a1 = createAssociation(productA, supplier1);

        ProductSupplier a2 = createAssociation(productA, supplier2);

        ProductSupplier b1 = createAssociation(productB, supplier1);

        Pageable pageable = PageRequest.of(0, 20);

        var all = repository.findAllFiltered(
                null,
                null,
                pageable
        );

        var byProduct = repository.findAllFiltered(
                productA.getId(),
                null,
                pageable
        );

        var bySupplier = repository.findAllFiltered(
                null,
                supplier1.getId(),
                pageable
        );

        var combined = repository.findAllFiltered(
                productA.getId(),
                supplier1.getId(),
                pageable
        );

        assertThat(all.getContent()).hasSize(3);

        assertThat(byProduct.getContent())
                .extracting(ProductSupplier::getId)
                .containsExactlyInAnyOrder(
                        a1.getId(),
                        a2.getId()
                );

        assertThat(bySupplier.getContent())
                .extracting(ProductSupplier::getId)
                .containsExactlyInAnyOrder(
                        a1.getId(),
                        b1.getId()
                );

        assertThat(combined.getContent())
                .extracting(ProductSupplier::getId)
                .containsExactly(a1.getId());
    }



    @Test
    void shouldFetchProductAndSupplierWithRelations() {
        Category category = createCategory();
        Product product = createProduct("SKU-1", category);
        Supplier supplier = createSupplier("supplier@email.com");

        ProductSupplier relation = createAssociation(product, supplier);

        entityManager.clear();

        ProductSupplier found = repository
                .findWithRelationsById(relation.getId())
                .orElseThrow();

        assertThat(Hibernate.isInitialized(found.getProduct())).isTrue();
        assertThat(Hibernate.isInitialized(found.getSupplier())).isTrue();
    }


    @Test
    void shouldPersistPurchasePriceUpdate() {
        Category category = createCategory();
        Product product = createProduct("SKU-1", category);
        Supplier supplier = createSupplier("supplier@email.com");

        ProductSupplier relation = createAssociation(product, supplier);

        relation.updatePurchasePrice(new BigDecimal("95.50"));
        entityManager.flush();
        entityManager.clear();

        ProductSupplier found = repository.findWithRelationsById(relation.getId())
                .orElseThrow();

        assertThat(found.getPurchasePrice()).isEqualByComparingTo("95.50");
    }


    @Test
    void shouldPreventDeletingProductWithSupplierAssociation() {
        Category category = createCategory();
        Product product = createProduct("SKU-1", category);
        Supplier supplier = createSupplier("supplier@email.com");

        createAssociation(product, supplier);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        "DELETE FROM products WHERE id = ?",
                        product.getId()
                )
        );
    }


    @Test
    void shouldPreventDeletingSupplierWithProductAssociation() {
        Category category = createCategory();
        Product product = createProduct("SKU-1", category);
        Supplier supplier = createSupplier("supplier@email.com");

        createAssociation(product, supplier);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        "DELETE FROM suppliers WHERE id = ?",
                        supplier.getId()
                )
        );
    }
}
