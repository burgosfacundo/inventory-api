package com.burgosfacundo.inventory.category.exception;

import com.burgosfacundo.inventory.common.exception.ResourceNotFoundException;

public class CategoryNotFoundException extends ResourceNotFoundException {
    public CategoryNotFoundException(Long id) {
        super(
                "Category not found with id: " + id,
                "CATEGORY_NOT_FOUND"
        );
    }
}
