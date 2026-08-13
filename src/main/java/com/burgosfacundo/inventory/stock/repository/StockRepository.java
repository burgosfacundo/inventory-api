package com.burgosfacundo.inventory.stock.repository;

import com.burgosfacundo.inventory.stock.model.Stock;
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
public interface StockRepository extends JpaRepository<Stock, Long> {

    boolean existsByProductIdAndWarehouseId(
            Long productId,
            Long warehouseId
    );

    @EntityGraph(
            attributePaths = {
                    "product",
                    "warehouse"
            }
    )
    Optional<Stock> findWithRelationsById(
            @Param("id") Long id
    );

    @EntityGraph(
            attributePaths = {
                    "product",
                    "warehouse"
            }
    )
    @Query("""
            SELECT s
            FROM Stock s
            WHERE (:productId IS NULL
                    OR s.product.id = :productId)
              AND (:warehouseId IS NULL
                    OR s.warehouse.id = :warehouseId)
    """)
    Page<Stock> findAllFiltered(
            @Param("productId")
            @Nullable Long productId,

            @Param("warehouseId")
            @Nullable Long warehouseId,

            Pageable pageable
    );


    @EntityGraph(attributePaths = {"product", "warehouse"})
    @Query("""
        SELECT s
        FROM Stock s
        WHERE s.quantity <= s.minimumStock
          AND (:warehouseId IS NULL
               OR s.warehouse.id = :warehouseId)
    """)
    Page<Stock> findLowStock(
            @Param("warehouseId")
            @Nullable Long warehouseId,
            Pageable pageable
    );
}
