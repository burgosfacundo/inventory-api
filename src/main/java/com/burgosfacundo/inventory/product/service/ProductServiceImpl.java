package com.burgosfacundo.inventory.product.service;

import com.burgosfacundo.inventory.category.exception.CategoryNotFoundException;
import com.burgosfacundo.inventory.category.repository.CategoryRepository;
import com.burgosfacundo.inventory.product.dto.ProductRequest;
import com.burgosfacundo.inventory.product.dto.ProductResponse;
import com.burgosfacundo.inventory.product.exception.ProductNotFoundException;
import com.burgosfacundo.inventory.product.exception.ProductSkuAlreadyExistsException;
import com.burgosfacundo.inventory.product.mapper.ProductMapper;
import com.burgosfacundo.inventory.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public ProductResponse save(ProductRequest request) {
        if (repository.existsBySku(request.sku())) {
            throw new ProductSkuAlreadyExistsException(request.sku());
        }

        var idCategory = request.categoryId();
        var category = categoryRepository.findById(idCategory)
                .orElseThrow(() -> new CategoryNotFoundException(idCategory));

        var product = repository.save(ProductMapper.toEntity(request, category));
        return ProductMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductResponse findById(Long id) {
        var product = repository.findWithCategoryById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return ProductMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductResponse> findAll(Long categoryId, Boolean active, Pageable pageable) {
        return repository
                .findAllFiltered(categoryId, active, pageable)
                .map(ProductMapper::toResponse);
    }

    @Transactional
    @Override
    public ProductResponse update(Long id,ProductRequest request) {
        var product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (repository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new ProductSkuAlreadyExistsException(request.sku());
        }

        var idCategory = request.categoryId();
        var category = categoryRepository.findById(idCategory)
                .orElseThrow(() -> new CategoryNotFoundException(idCategory));

        product.update(
                request.sku(),
                request.name(),
                request.description(),
                request.salePrice(),
                category
        );

        return ProductMapper.toResponse(product);
    }

    @Transactional
    @Override
    public ProductResponse updateStatus(Long id, boolean active) {
        var product = repository.findWithCategoryById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (active) {
            product.activate();
        } else {
            product.deactivate();
        }

        return ProductMapper.toResponse(product);
    }

    @Transactional
    @Override
    public void delete(Long id) {
       if (!repository.existsById(id)) {
           throw new ProductNotFoundException(id);
       }
        repository.deleteById(id);
    }
}
