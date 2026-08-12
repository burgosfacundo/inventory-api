package com.burgosfacundo.inventory.warehouse.service;

import com.burgosfacundo.inventory.warehouse.dto.WarehouseRequest;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WarehouseService {

    WarehouseResponse save(WarehouseRequest request);

    WarehouseResponse findById(Long id);

    Page<WarehouseResponse> findAll(Pageable pageable);

    WarehouseResponse update(
            Long id,
            WarehouseRequest request
    );

    void delete(Long id);
}
