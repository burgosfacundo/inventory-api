package com.burgosfacundo.inventory.product.repository;

import com.burgosfacundo.inventory.product.model.Product;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@NullMarked
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryById(Long id);

    @EntityGraph(attributePaths = "category")
    @Query("""
        SELECT p
        FROM Product p
        WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:active IS NULL OR p.active = :active)
    """)
    Page<Product> findAllFiltered(
            @Param("categoryId") @Nullable Long categoryId,
            @Param("active") @Nullable Boolean active,
            Pageable pageable
    );

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);
}
