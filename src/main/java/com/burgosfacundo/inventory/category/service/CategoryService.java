package com.burgosfacundo.inventory.category.service;

import com.burgosfacundo.inventory.category.dto.CategoryRequest;
import com.burgosfacundo.inventory.category.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    CategoryResponse findById(Long id);
    List<CategoryResponse> findAll();
    CategoryResponse update(Long id, CategoryRequest request);
}
