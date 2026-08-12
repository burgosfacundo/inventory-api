package com.burgosfacundo.inventory.warehouse.repository;

import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface WarehouseRepository
        extends JpaRepository<Warehouse, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(
            String code,
            Long id
    );
}
