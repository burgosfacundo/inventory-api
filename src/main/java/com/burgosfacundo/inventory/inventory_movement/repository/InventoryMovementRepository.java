package com.burgosfacundo.inventory.inventory_movement.repository;

import com.burgosfacundo.inventory.inventory_movement.model.InventoryMovement;
import com.burgosfacundo.inventory.inventory_movement.model.MovementType;
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
public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovement, Long> {

    @EntityGraph(attributePaths = {"product", "warehouse"})
    Optional<InventoryMovement> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    @Query("""
            SELECT m
            FROM InventoryMovement m
            WHERE (:productId IS NULL
                    OR m.product.id = :productId)
              AND (:warehouseId IS NULL
                    OR m.warehouse.id = :warehouseId)
              AND (:type IS NULL
                    OR m.type = :type)
              AND (:from IS NULL
                    OR m.createdAt >= :from)
              AND (:to IS NULL
                    OR m.createdAt <= :to)
    """)
    Page<InventoryMovement> findAllFiltered(
            @Param("productId")
            @Nullable Long productId,

            @Param("warehouseId")
            @Nullable Long warehouseId,

            @Param("type")
            @Nullable MovementType type,

            @Param("from")
            @Nullable LocalDateTime from,

            @Param("to")
            @Nullable LocalDateTime to,

            Pageable pageable
    );
}