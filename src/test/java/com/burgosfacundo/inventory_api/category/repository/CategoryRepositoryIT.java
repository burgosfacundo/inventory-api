package com.burgosfacundo.inventory_api.category.repository;


import com.burgosfacundo.inventory_api.category.entity.Category;
import com.burgosfacundo.inventory_api.config.IntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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


    @Test
    void shouldRejectCategoryWithoutName() {
        Category category = new Category(
                null,
                "Invalid category"
        );

        assertThatThrownBy(() -> categoryRepository.saveAndFlush(category))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
