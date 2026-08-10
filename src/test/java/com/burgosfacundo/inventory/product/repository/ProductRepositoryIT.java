package com.burgosfacundo.inventory.product.repository;


import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.category.repository.CategoryRepository;
import com.burgosfacundo.inventory.config.IntegrationTest;
import com.burgosfacundo.inventory.product.model.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Transactional
@SpringBootTest
public class ProductRepositoryIT extends IntegrationTest {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveProduct() {
        Category category =
                categoryRepository.saveAndFlush(
                        new Category("Name", "Description")
                );

        Product product = new Product(
                "SKU1",
                "Product",
                null,
                BigDecimal.valueOf(1),
                category
        );

        Product saved = productRepository.saveAndFlush(product);

        entityManager.clear();

        Product found = productRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getSku()).isEqualTo("SKU1");
        assertThat(found.getName()).isEqualTo("Product");
        assertThat(found.getDescription())
                .isEqualTo(null);
        assertThat(found.getCategory().getId()).isEqualTo(category.getId());
    }

    @Test
    void shouldDetectExistingSku() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Category", "Description")
        );

        Product product = new Product(
                "SKU123",
                "Product",
                null,
                new BigDecimal("10.00"),
                category
        );

        productRepository.saveAndFlush(product);

        assertThat(productRepository.existsBySku("SKU123"))
                .isTrue();

        assertThat(productRepository.existsBySku("OTHER-SKU"))
                .isFalse();
    }

    @Test
    void shouldDetectSkuUsedByAnotherProduct() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Category", "Description")
        );

        productRepository.saveAndFlush(
                new Product(
                        "SKU1",
                        "Product 1",
                        null,
                        new BigDecimal("10.00"),
                        category
                )
        );

        Product product2 = productRepository.saveAndFlush(
                new Product(
                        "SKU2",
                        "Product 2",
                        null,
                        new BigDecimal("20.00"),
                        category
                )
        );

        assertThat(
                productRepository.existsBySkuAndIdNot(
                        "SKU1",
                        product2.getId()
                )
        ).isTrue();
    }


    @Test
    void shouldNotConsiderOwnSkuAsDuplicate() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Category", "Description")
        );

        Product product = productRepository.saveAndFlush(
                new Product(
                        "SKU1",
                        "Product",
                        null,
                        new BigDecimal("10.00"),
                        category
                )
        );

        assertThat(
                productRepository.existsBySkuAndIdNot(
                        "SKU1",
                        product.getId()
                )
        ).isFalse();
    }
}
