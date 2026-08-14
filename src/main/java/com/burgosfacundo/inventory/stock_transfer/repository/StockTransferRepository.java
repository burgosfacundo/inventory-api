package com.burgosfacundo.inventory.stock_transfer.repository;

import com.burgosfacundo.inventory.stock_transfer.model.StockTransfer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@NullMarked
@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    @EntityGraph(attributePaths = {
            "product",
            "sourceWarehouse",
            "destinationWarehouse"}
    )
    Optional<StockTransfer> findWithRelationsById(Long id);

    @EntityGraph(
            attributePaths = {
                    "product",
                    "sourceWarehouse",
                    "destinationWarehouse"
            }
    )
    @Query("""
    SELECT t
    FROM StockTransfer t
    WHERE (:productId IS NULL OR t.product.id = :productId)
    AND (:sourceWarehouseId IS NULL OR t.sourceWarehouse.id = :sourceWarehouseId)
    AND (:destinationWarehouseId IS NULL OR t.destinationWarehouse.id = :destinationWarehouseId)
    AND (:from IS NULL OR t.createdAt >= :from)
    AND (:to IS NULL OR t.createdAt <= :to)
    """)
    Page<StockTransfer> findAllFiltered(@Param("productId")
                                        @Nullable Long productId,
                                        @Param("sourceWarehouseId")
                                        @Nullable Long sourceWarehouseId,
                                        @Param("destinationWarehouseId")
                                        @Nullable Long destinationWarehouseId,
                                        @Param("from")
                                        @Nullable LocalDateTime from,
                                        @Param("to")
                                        @Nullable LocalDateTime to,
                                        Pageable pageable);
}
