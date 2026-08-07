package com.burgosfacundo.inventory.category.repository;

import com.burgosfacundo.inventory.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
