package com.burgosfacundo.inventory.stock.service;

import com.burgosfacundo.inventory.stock.dto.StockMinimumRequest;
import com.burgosfacundo.inventory.stock.dto.StockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockService {

    StockResponse findById(Long id);

    Page<StockResponse> findAll(
            Long productId,
            Long warehouseId,
            Pageable pageable
    );

    Page<StockResponse> findLowStock(
            Long warehouseId,
            Pageable pageable
    );

    StockResponse updateMinimumStock(
            Long id,
            StockMinimumRequest request
    );
}