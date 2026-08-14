package com.burgosfacundo.inventory.stock_transfer.controller;

import com.burgosfacundo.inventory.common.web.SortUtils;
import com.burgosfacundo.inventory.stock_transfer.dto.StockTransferRequest;
import com.burgosfacundo.inventory.stock_transfer.dto.StockTransferResponse;
import com.burgosfacundo.inventory.stock_transfer.service.StockTransferService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("/stock-transfers")
@RequiredArgsConstructor
public class StockTransferController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "quantity",
            "createdAt",
            "product.id",
            "sourceWarehouse.id",
            "destinationWarehouse.id"
    );

    private final StockTransferService service;

    @PostMapping
    public ResponseEntity<StockTransferResponse> create(@RequestBody @Valid StockTransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockTransferResponse> findById(
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<StockTransferResponse>> findAll(
            @RequestParam(required = false)
            @Positive(message = "Product id must be positive")
            Long productId,

            @RequestParam(required = false)
            @Positive(message = "Source warehouse id must be positive")
            Long sourceWarehouseId,

            @RequestParam(required = false)
            @Positive(message = "Destination warehouse id must be positive")
            Long destinationWarehouseId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,

            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "Number page cannot be negative")
            Integer page,

            @RequestParam(defaultValue = "20")
            @Positive(message = "Size page must be positive")
            @Max(value = 100, message = "Size cannot be greater than 100")
            Integer size,

            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction) {
        Pageable pageable = PageRequest.of(page, size, SortUtils.build(sortBy, direction, ALLOWED_SORT_FIELDS));

        return ResponseEntity.ok(service.findAll(productId, sourceWarehouseId, destinationWarehouseId, from, to, pageable));
    }
}