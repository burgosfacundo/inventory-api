package com.burgosfacundo.inventory.category.exception;

import com.burgosfacundo.inventory.common.exception.ResourceNotFoundException;

public class CategoryNotFoundException extends ResourceNotFoundException {
    public CategoryNotFoundException(Long id) {
        super("Category with id " + id + " not found");
    }
}
