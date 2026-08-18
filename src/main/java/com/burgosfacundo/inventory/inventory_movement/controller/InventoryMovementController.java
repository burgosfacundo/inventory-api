package com.burgosfacundo.inventory.inventory_movement.controller;

import com.burgosfacundo.inventory.common.web.SortUtils;
import com.burgosfacundo.inventory.inventory_movement.dto.InventoryMovementRequest;
import com.burgosfacundo.inventory.inventory_movement.dto.InventoryMovementResponse;
import com.burgosfacundo.inventory.inventory_movement.model.MovementType;
import com.burgosfacundo.inventory.inventory_movement.service.InventoryMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/inventory-movements")
@Tag(
        name = "Inventory Movements",
        description = "Register and query traceable IN and OUT inventory movements."
)
@RequiredArgsConstructor
public class InventoryMovementController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "quantity",
            "createdAt",
            "product.id",
            "warehouse.id"
    );

    private final InventoryMovementService service;

    @Operation(
            summary = "Register an inventory movement",
            description = "Registers an IN or OUT movement and updates the corresponding stock atomically."
    )
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    @PostMapping
    public ResponseEntity<InventoryMovementResponse> create(
            @RequestBody
            @Valid
            InventoryMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Get inventory movement by id")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @GetMapping("/{id}")
    public ResponseEntity<InventoryMovementResponse> findById(
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(
            summary = "List inventory movements",
            description = "Returns movement history with optional product, warehouse, type and date-range filters."
    )
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @GetMapping
    public ResponseEntity<Page<InventoryMovementResponse>> findAll(
            @RequestParam(required = false)
            @Positive(message = "Product id must be positive")
            Long productId,

            @RequestParam(required = false)
            @Positive(message = "Warehouse id must be positive")
            Long warehouseId,

            @RequestParam(required = false)
            MovementType type,

            @Parameter(
                    description = "Start of the creation date range in ISO-8601 format",
                    example = "2026-08-01T00:00:00"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @Parameter(
                    description = "End of the creation date range in ISO-8601 format",
                    example = "2026-08-31T23:59:59"
            )
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

            @Parameter(
                    description = "Field used for sorting",
                    schema = @Schema(
                            allowableValues = {
                                    "id",
                                    "quantity",
                                    "createdAt",
                                    "product.id",
                                    "warehouse.id"
                            }
                    )
            )
            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @Parameter(
                    description = "Sort direction",
                    schema = @Schema(allowableValues = {"asc", "desc"})
            )
            @RequestParam(defaultValue = "desc")
            String direction) {
        Pageable pageable = PageRequest.of(page, size, SortUtils.build(sortBy, direction, ALLOWED_SORT_FIELDS));

        return ResponseEntity.ok(service.findAll(productId, warehouseId, type, from, to, pageable));
    }
}