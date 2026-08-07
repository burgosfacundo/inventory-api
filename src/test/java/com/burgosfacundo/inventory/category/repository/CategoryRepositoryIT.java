package com.burgosfacundo.inventory.category.repository;


import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.config.IntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class CategoryRepositoryIT extends IntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveCategory() {
        Category category = new Category(
                "Electronics",
                "Electronic devices and accessories"
        );

        Category saved = categoryRepository.saveAndFlush(category);

        entityManager.clear();

        Category found = categoryRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("Electronics");
        assertThat(found.getDescription())
                .isEqualTo("Electronic devices and accessories");
    }
}
