package com.burgosfacundo.inventory.stock_transfer.service;

import com.burgosfacundo.inventory.inventory_movement.model.InventoryMovement;
import com.burgosfacundo.inventory.inventory_movement.model.MovementType;
import com.burgosfacundo.inventory.inventory_movement.repository.InventoryMovementRepository;
import com.burgosfacundo.inventory.product.exception.ProductNotFoundException;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product.repository.ProductRepository;
import com.burgosfacundo.inventory.stock.exception.StockNotFoundException;
import com.burgosfacundo.inventory.stock.model.Stock;
import com.burgosfacundo.inventory.stock.repository.StockRepository;
import com.burgosfacundo.inventory.stock_transfer.dto.StockTransferRequest;
import com.burgosfacundo.inventory.stock_transfer.dto.StockTransferResponse;
import com.burgosfacundo.inventory.stock_transfer.exception.InvalidTransferDateRangeException;
import com.burgosfacundo.inventory.stock_transfer.exception.SameWarehouseTransferException;
import com.burgosfacundo.inventory.stock_transfer.exception.StockTransferNotFoundException;
import com.burgosfacundo.inventory.stock_transfer.mapper.StockTransferMapper;
import com.burgosfacundo.inventory.stock_transfer.repository.StockTransferRepository;
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
public class StockTransferServiceImpl implements StockTransferService {
    private final StockTransferRepository repository;
    private final StockRepository stockRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional
    @Override
    public StockTransferResponse create(StockTransferRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        Warehouse source = warehouseRepository.findById(request.sourceWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(request.sourceWarehouseId()));

        Warehouse destination = warehouseRepository.findById(request.destinationWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(request.destinationWarehouseId()));

        validateDifferentWarehouses(source, destination);

        // Sort and find stocks
        LockedStocks stocks = lockStocks(product.getId(), source,destination);

        //Check and move stock
        moveStock(product,source,destination,request.quantity(),stocks);

        //Save movements of inventory
        registerMovements(product,source,destination,request.quantity());

        var saved = repository.save(StockTransferMapper.toEntity(product,source,destination,request.quantity()));

        return StockTransferMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public StockTransferResponse findById(Long id) {
        var transfer = repository.findWithRelationsById(id)
                .orElseThrow(() -> new StockTransferNotFoundException(id));
        return  StockTransferMapper.toResponse(transfer);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<StockTransferResponse> findAll(Long productId, Long sourceWarehouseId, Long destinationWarehouseId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        validateDateRange(from, to);
        return repository.findAllFiltered(productId,sourceWarehouseId,destinationWarehouseId,from,to,pageable)
                .map(StockTransferMapper::toResponse);
    }

    private static void validateDifferentWarehouses(Warehouse sourceWarehouse, Warehouse destinationWarehouse) {
        if (sourceWarehouse.getId().equals(destinationWarehouse.getId())) {
            throw new SameWarehouseTransferException();
        }
    }

    private static void validateDateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidTransferDateRangeException();
        }
    }


    /**
     * Method for sort and lock stocks
     * @param productId product of transfer
     * @param source source warehouse
     * @param destination destination warehouse
     * @return record with source stock and destination stock
     */
    private LockedStocks lockStocks(Long productId, Warehouse source, Warehouse destination) {
        Long firstWarehouseId = Math.min(source.getId(), destination.getId());

        Long secondWarehouseId = Math.max(source.getId(), destination.getId());

        Optional<Stock> firstStock = stockRepository.findByProductIdAndWarehouseIdForUpdate(productId, firstWarehouseId);

        Optional<Stock> secondStock = stockRepository.findByProductIdAndWarehouseIdForUpdate(productId, secondWarehouseId);

        if (source.getId().equals(firstWarehouseId)) {
            return new LockedStocks(firstStock, secondStock);
        }

        return new LockedStocks(secondStock, firstStock);
    }

    private record LockedStocks(Optional<Stock> source, Optional<Stock> destination) { }


    /**
     * Method to check stock availability and move quantities.
     * @param product product of transfer
     * @param source source warehouse
     * @param destination destination warehouse
     * @param quantity to transfer
     * @param stocks locked stocks of warehouses
     */
    private void moveStock(Product product, Warehouse source, Warehouse destination, int quantity, LockedStocks stocks) {
        // Stock must exist at the source warehouse
        Stock sourceStock = stocks.source()
                .orElseThrow(() -> new StockNotFoundException(product.getId(), source.getId()));
        sourceStock.decrease(quantity);

        // Stock in warehouse destination might not exist
        if (stocks.destination().isPresent()) {
            stocks.destination().get().increase(quantity);
            return;
        }

        Stock destinationStock = new Stock(product, destination, quantity, 0);
        stockRepository.save(destinationStock);
    }

    /**
     * Method for saving stock transfer movements between warehouses.
     * @param product of transfer
     * @param source source warehouse
     * @param destination destination warehouse
     * @param quantity of transfer
     */
    private void registerMovements(Product product, Warehouse source, Warehouse destination, int quantity) {
        InventoryMovement outMovement = new InventoryMovement(product, source, MovementType.OUT, quantity);

        InventoryMovement inMovement = new InventoryMovement(product, destination, MovementType.IN, quantity);

        movementRepository.save(outMovement);
        movementRepository.save(inMovement);
    }
}
