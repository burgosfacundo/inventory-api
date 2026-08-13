package com.burgosfacundo.inventory.warehouse.controller;

import com.burgosfacundo.inventory.common.web.SortUtils;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseRequest;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseResponse;
import com.burgosfacundo.inventory.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "code",
            "name"
    );

    private final WarehouseService service;

    @PostMapping
    public ResponseEntity<WarehouseResponse> create(
            @RequestBody
            @Valid
            WarehouseRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.save(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponse> findById(
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id) {
        return ResponseEntity
                .ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<WarehouseResponse>> findAll(
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
        Pageable pageable = PageRequest.of(page, size, SortUtils.build(sortBy,direction,ALLOWED_SORT_FIELDS));
        return ResponseEntity
                .ok(service.findAll(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponse> update(
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id,
            @RequestBody
            @Valid
            WarehouseRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WarehouseResponse> delete(
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id){
        service.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
