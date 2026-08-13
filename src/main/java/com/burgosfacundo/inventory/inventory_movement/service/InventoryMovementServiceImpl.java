package com.burgosfacundo.inventory.inventory_movement.service;

import com.burgosfacundo.inventory.inventory_movement.dto.InventoryMovementRequest;
import com.burgosfacundo.inventory.inventory_movement.dto.InventoryMovementResponse;
import com.burgosfacundo.inventory.inventory_movement.exception.InvalidMovementDateRangeException;
import com.burgosfacundo.inventory.inventory_movement.exception.InventoryMovementNotFoundException;
import com.burgosfacundo.inventory.inventory_movement.mapper.InventoryMovementMapper;
import com.burgosfacundo.inventory.inventory_movement.model.InventoryMovement;
import com.burgosfacundo.inventory.inventory_movement.model.MovementType;
import com.burgosfacundo.inventory.inventory_movement.repository.InventoryMovementRepository;
import com.burgosfacundo.inventory.product.exception.ProductNotFoundException;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product.repository.ProductRepository;
import com.burgosfacundo.inventory.stock.exception.StockNotFoundException;
import com.burgosfacundo.inventory.stock.model.Stock;
import com.burgosfacundo.inventory.stock.repository.StockRepository;
import com.burgosfacundo.inventory.warehouse.exception.WarehouseNotFoundException;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import com.burgosfacundo.inventory.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional
    @Override
    public InventoryMovementResponse create(InventoryMovementRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(request.warehouseId()));


        InventoryMovement movement = InventoryMovementMapper.toEntity(request, product, warehouse);

        Optional<Stock> existingStock = stockRepository.findByProductIdAndWarehouseIdForUpdate(product.getId(), warehouse.getId());

        if (request.type() == MovementType.IN) {
            processIn(product, warehouse, request.quantity(), existingStock);
        } else {
            processOut(product, warehouse, request.quantity(), existingStock);
        }

        InventoryMovement saved = movementRepository.save(movement);

        return InventoryMovementMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public InventoryMovementResponse findById(Long id) {
        InventoryMovement movement = movementRepository.findWithRelationsById(id)
                .orElseThrow(() -> new InventoryMovementNotFoundException(id));

        return InventoryMovementMapper.toResponse(movement);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<InventoryMovementResponse> findAll(Long productId, Long warehouseId, MovementType type, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        validateDateRange(from, to);
        return movementRepository.findAllFiltered(productId, warehouseId, type, from, to, pageable)
                .map(InventoryMovementMapper::toResponse);
    }

    private void processIn(Product product, Warehouse warehouse, int quantity, Optional<Stock> existingStock) {
        if (existingStock.isPresent()) {
            existingStock.get().increase(quantity);
            return;
        }

        Stock stock = new Stock(product, warehouse, quantity, 0);

        stockRepository.save(stock);
    }

    private void processOut(Product product, Warehouse warehouse, int quantity, Optional<Stock> existingStock) {
        Stock stock = existingStock
                .orElseThrow(() -> new StockNotFoundException(product.getId(), warehouse.getId()));

        stock.decrease(quantity);
    }

    private static void validateDateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidMovementDateRangeException();
        }
    }
}