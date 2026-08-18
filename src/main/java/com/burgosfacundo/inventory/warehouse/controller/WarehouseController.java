package com.burgosfacundo.inventory.warehouse.controller;

import com.burgosfacundo.inventory.common.web.SortUtils;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseRequest;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseResponse;
import com.burgosfacundo.inventory.warehouse.service.WarehouseService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/warehouses")
@Tag(
        name = "Warehouses",
        description = "Manage warehouses and their addresses."
)
@RequiredArgsConstructor
public class WarehouseController {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "code",
            "name"
    );

    private final WarehouseService service;

    @Operation(
            summary = "Create a warehouse",
            description = "Creates a warehouse after validating its address."
    )
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    @ApiResponse(responseCode = "422", ref = "#/components/responses/UnprocessableContent")
    @ApiResponse(responseCode = "503", ref = "#/components/responses/ServiceUnavailable")
    @PostMapping
    public ResponseEntity<WarehouseResponse> create(
            @RequestBody
            @Valid
            WarehouseRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.save(request));
    }

    @Operation(summary = "Get warehouse by id")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponse> findById(
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id) {
        return ResponseEntity
                .ok(service.findById(id));
    }

    @Operation(summary = "List warehouses")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @GetMapping
    public ResponseEntity<Page<WarehouseResponse>> findAll(
            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "Number page cannot be negative")
            Integer page,
            @RequestParam(defaultValue = "20")
            @Positive(message = "Size page must be positive")
            @Max(value = 100, message = "Size cannot be greater than 100")
            Integer size,

            @Parameter(
                    description = "Field used for sorting",
                    schema = @Schema(allowableValues = {"id", "code", "name"})
            )
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(
                    description = "Sort direction",
                    schema = @Schema(allowableValues = {"asc", "desc"})
            )
            @RequestParam(defaultValue = "asc") String direction
    ){
        Pageable pageable = PageRequest.of(page, size, SortUtils.build(sortBy,direction,ALLOWED_SORT_FIELDS));
        return ResponseEntity
                .ok(service.findAll(pageable));
    }

    @Operation(
            summary = "Update a warehouse",
            description = "Updates warehouse data after validating its address."
    )
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    @ApiResponse(responseCode = "422", ref = "#/components/responses/UnprocessableContent")
    @ApiResponse(responseCode = "503", ref = "#/components/responses/ServiceUnavailable")
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

    @Operation(summary = "Delete a warehouse")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id){
        service.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
