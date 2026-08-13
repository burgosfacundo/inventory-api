package com.burgosfacundo.inventory.product.mapper;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.category.dto.CategorySummaryResponse;
import com.burgosfacundo.inventory.product.dto.ProductRequest;
import com.burgosfacundo.inventory.product.dto.ProductResponse;
import com.burgosfacundo.inventory.product.model.Product;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductMapper {

    public static Product toEntity(ProductRequest request, Category category) {
        return new Product(
                request.sku(),
                request.name(),
                request.description(),
                request.salePrice(),
                category
        );
    }

    public static ProductResponse toResponse(Product product) {
        var category = product.getCategory();
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getSalePrice(),
                product.getActive(),
                new CategorySummaryResponse(
                        category.getId(),
                        category.getName())
                );
    }
}
