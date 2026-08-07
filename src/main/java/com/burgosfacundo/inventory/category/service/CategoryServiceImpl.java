package com.burgosfacundo.inventory.category.service;

import com.burgosfacundo.inventory.category.dto.CategoryRequest;
import com.burgosfacundo.inventory.category.dto.CategoryResponse;
import com.burgosfacundo.inventory.category.exception.CategoryNotFoundException;
import com.burgosfacundo.inventory.category.mapper.CategoryMapper;
import com.burgosfacundo.inventory.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;


    @Transactional
    @Override
    public CategoryResponse create(CategoryRequest request) {
        var category = CategoryMapper.toEntity(request);
        var savedCategory = repository.save(category);
        return CategoryMapper.toResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryResponse findById(Long id) {
        return CategoryMapper.toResponse(repository.findById(id).orElseThrow(
                ()-> new CategoryNotFoundException(id)
        ));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> findAll() {
        return repository.findAll().stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        var category = repository.findById(id).orElseThrow(
                ()-> new CategoryNotFoundException(id)
        );
        category.update(request.name(), request.description());
        return CategoryMapper.toResponse(category);
    }
}
