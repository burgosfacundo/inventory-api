package com.burgosfacundo.inventory.category.mapper;

import com.burgosfacundo.inventory.category.dto.CategoryRequest;
import com.burgosfacundo.inventory.category.dto.CategoryResponse;
import com.burgosfacundo.inventory.category.entity.Category;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMapper {

    public static Category toEntity(CategoryRequest categoryRequest) {
        return new Category(categoryRequest.name(), categoryRequest.description());
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
