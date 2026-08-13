package com.burgosfacundo.inventory.stock;

import com.burgosfacundo.inventory.common.web.SortUtils;
import com.burgosfacundo.inventory.stock.dto.StockMinimumRequest;
import com.burgosfacundo.inventory.stock.dto.StockResponse;
import com.burgosfacundo.inventory.stock.service.StockService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "quantity",
            "minimumStock",
            "product.id",
            "warehouse.id"
    );

    private final StockService service;

    @GetMapping("/{id}")
    public ResponseEntity<StockResponse> findById(
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<StockResponse>> findAll(
            @RequestParam(required = false)
            @Positive(message = "Product id must be positive")
            Long productId,
            @RequestParam(required = false)
            @Positive(message = "Warehouse id must be positive")
            Long warehouseId,

            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "Number page cannot be negative")
            Integer page,
            @RequestParam(defaultValue = "20")
            @Positive(message = "Size page must be positive")
            @Max(value = 100, message = "Size cannot be greater than 100")
            Integer size,

            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        Pageable pageable = PageRequest.of(page, size, SortUtils.build(sortBy, direction, ALLOWED_SORT_FIELDS));
        return ResponseEntity.ok(service.findAll(productId, warehouseId, pageable));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Page<StockResponse>> findAllLowStock(
            @RequestParam(required = false)
            @Positive(message = "Warehouse id must be positive")
            Long warehouseId,

            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "Number page cannot be negative")
            Integer page,
            @RequestParam(defaultValue = "20")
            @Positive(message = "Size page must be positive")
            @Max(value = 100, message = "Size cannot be greater than 100")
            Integer size,

            @RequestParam(defaultValue = "id")
            String sortBy,
            @RequestParam(defaultValue = "asc")
            String direction
    ){
        Pageable pageable = PageRequest.of(page,size,SortUtils.build(sortBy, direction, ALLOWED_SORT_FIELDS));
        return ResponseEntity.ok(service.findLowStock(warehouseId, pageable));
    }

    @PatchMapping("/{id}/minimum-stock")
    public ResponseEntity<StockResponse> updateMinimumStock(
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id,

            @RequestBody
            @Valid
            StockMinimumRequest request
    ) {
        return ResponseEntity.ok(service.updateMinimumStock(id, request));
    }

}
