package com.burgosfacundo.inventory_api.category.repository;

import com.burgosfacundo.inventory_api.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
