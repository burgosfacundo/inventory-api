package com.burgosfacundo.inventory.supplier.repository;

import com.burgosfacundo.inventory.supplier.model.Supplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("""
        SELECT s
        FROM Supplier s
        WHERE (:active IS NULL OR s.active = :active)
    """)
    Page<Supplier> findAllFiltered(
            @Param("active") @Nullable Boolean active,
            Pageable pageable
    );
}
