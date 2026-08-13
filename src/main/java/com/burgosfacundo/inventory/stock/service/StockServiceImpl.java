package com.burgosfacundo.inventory.stock.service;

import com.burgosfacundo.inventory.stock.dto.StockMinimumRequest;
import com.burgosfacundo.inventory.stock.dto.StockResponse;
import com.burgosfacundo.inventory.stock.exception.StockNotFoundException;
import com.burgosfacundo.inventory.stock.mapper.StockMapper;
import com.burgosfacundo.inventory.stock.model.Stock;
import com.burgosfacundo.inventory.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockServiceImpl
        implements StockService {

    private final StockRepository repository;

    @Transactional(readOnly = true)
    @Override
    public StockResponse findById(Long id) {
        Stock stock =
                repository.findWithRelationsById(id)
                        .orElseThrow(
                                () -> new StockNotFoundException(id)
                        );

        return StockMapper.toResponse(stock);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<StockResponse> findAll(
            Long productId,
            Long warehouseId,
            Pageable pageable
    ) {
        return repository
                .findAllFiltered(
                        productId,
                        warehouseId,
                        pageable
                )
                .map(StockMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<StockResponse> findLowStock(
            Long warehouseId,
            Pageable pageable
    ) {
        return repository
                .findLowStock(
                        warehouseId,
                        pageable
                )
                .map(StockMapper::toResponse);
    }

    @Transactional
    @Override
    public StockResponse updateMinimumStock(
            Long id,
            StockMinimumRequest request
    ) {
        Stock stock =
                repository.findWithRelationsById(id)
                        .orElseThrow(
                                () -> new StockNotFoundException(id)
                        );

        stock.updateMinimumStock(
                request.minimumStock()
        );

        return StockMapper.toResponse(stock);
    }
}