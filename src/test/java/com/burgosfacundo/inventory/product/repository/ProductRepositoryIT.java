package com.burgosfacundo.inventory.product.repository;


import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.category.repository.CategoryRepository;
import com.burgosfacundo.inventory.config.IntegrationTest;
import com.burgosfacundo.inventory.product.model.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class ProductRepositoryIT extends IntegrationTest {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveCategory() {
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
}
