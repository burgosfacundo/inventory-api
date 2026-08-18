package com.burgosfacundo.inventory.stock.controller;

import com.burgosfacundo.inventory.common.web.SortUtils;
import com.burgosfacundo.inventory.stock.dto.StockMinimumRequest;
import com.burgosfacundo.inventory.stock.dto.StockResponse;
import com.burgosfacundo.inventory.stock.service.StockService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/stocks")
@Tag(
        name = "Stocks",
        description = "Read stock levels and configure minimum stock thresholds. Stock quantities are changed through inventory movements and transfers."
)
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

    @Operation(summary = "Get stock by id")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @GetMapping("/{id}")
    public ResponseEntity<StockResponse> findById(
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(
            summary = "List stocks",
            description = "Returns paginated stock records with optional product and warehouse filters."
    )
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
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

            @Parameter(
                    description = "Field used for sorting",
                    schema = @Schema(
                            allowableValues = {
                                    "id",
                                    "quantity",
                                    "minimumStock",
                                    "product.id",
                                    "warehouse.id"
                            }
                    )
            )
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(
                    description = "Sort direction",
                    schema = @Schema(allowableValues = {"asc", "desc"})
            )
            @RequestParam(defaultValue = "asc") String direction
    ){
        Pageable pageable = PageRequest.of(page, size, SortUtils.build(sortBy, direction, ALLOWED_SORT_FIELDS));
        return ResponseEntity.ok(service.findAll(productId, warehouseId, pageable));
    }

    @Operation(
            summary = "List low-stock records",
            description = "Returns stock records identified as low stock, optionally filtered by warehouse."
    )
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
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

            @Parameter(
                    description = "Field used for sorting",
                    schema = @Schema(
                            allowableValues = {
                                    "id",
                                    "quantity",
                                    "minimumStock",
                                    "product.id",
                                    "warehouse.id"
                            }
                    )
            )
            @RequestParam(defaultValue = "id")
            String sortBy,
            @Parameter(
                    description = "Sort direction",
                    schema = @Schema(allowableValues = {"asc", "desc"})
            )
            @RequestParam(defaultValue = "asc")
            String direction
    ){
        Pageable pageable = PageRequest.of(page,size,SortUtils.build(sortBy, direction, ALLOWED_SORT_FIELDS));
        return ResponseEntity.ok(service.findLowStock(warehouseId, pageable));
    }

    @Operation(summary = "Update minimum stock")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
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
