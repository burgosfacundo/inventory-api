package com.burgosfacundo.inventory.stock_transfer.service;

import com.burgosfacundo.inventory.stock_transfer.dto.StockTransferRequest;
import com.burgosfacundo.inventory.stock_transfer.dto.StockTransferResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface StockTransferService {

    StockTransferResponse create(StockTransferRequest request);

    StockTransferResponse findById(Long id);

    Page<StockTransferResponse> findAll(Long productId, Long sourceWarehouseId, Long destinationWarehouseId,
            LocalDateTime from, LocalDateTime to, Pageable pageable);
}