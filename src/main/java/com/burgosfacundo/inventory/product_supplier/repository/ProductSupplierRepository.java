package com.burgosfacundo.inventory.product_supplier.repository;

import com.burgosfacundo.inventory.product_supplier.model.ProductSupplier;
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
public interface ProductSupplierRepository extends JpaRepository<ProductSupplier, Long> {
    boolean existsByProductIdAndSupplierId(
            Long productId,
            Long supplierId
    );

    @EntityGraph(attributePaths = {"product", "supplier"})
    Optional<ProductSupplier> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"product", "supplier"})
    @Query("""
        SELECT ps
        FROM ProductSupplier ps
        WHERE (:productId IS NULL OR ps.product.id = :productId)
          AND (:supplierId IS NULL OR ps.supplier.id = :supplierId)
    """)
    Page<ProductSupplier> findAllFiltered(
            @Param("productId") @Nullable Long productId,
            @Param("supplierId") @Nullable Long supplierId,
            Pageable pageable
    );
}
