package com.burgosfacundo.inventory.inventory_movement.service;

import com.burgosfacundo.inventory.inventory_movement.dto.InventoryMovementRequest;
import com.burgosfacundo.inventory.inventory_movement.dto.InventoryMovementResponse;
import com.burgosfacundo.inventory.inventory_movement.model.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface InventoryMovementService {

    InventoryMovementResponse create(InventoryMovementRequest request);

    InventoryMovementResponse findById(Long id);

    Page<InventoryMovementResponse> findAll(Long productId, Long warehouseId, MovementType type,
                                            LocalDateTime from, LocalDateTime to, Pageable pageable);
}