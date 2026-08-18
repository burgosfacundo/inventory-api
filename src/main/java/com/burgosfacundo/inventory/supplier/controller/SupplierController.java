package com.burgosfacundo.inventory.supplier.controller;

import com.burgosfacundo.inventory.common.web.SortUtils;
import com.burgosfacundo.inventory.supplier.dto.SupplierRequest;
import com.burgosfacundo.inventory.supplier.dto.SupplierResponse;
import com.burgosfacundo.inventory.supplier.service.SupplierService;
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
@RequestMapping("/suppliers")
@Tag(
        name = "Suppliers",
        description = "Manage suppliers and their activation status."
)
@RequiredArgsConstructor
public class SupplierController {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "name",
            "email",
            "active"
    );

    private final SupplierService service;

    @Operation(summary = "Create a supplier")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    @PostMapping
    public ResponseEntity<SupplierResponse> save(@RequestBody @Valid SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(request));
    }

    @Operation(summary = "Get supplier by id")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> findById(
                                                        @PathVariable
                                                        @Positive(message = "Id must be positive")
                                                        Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(
            summary = "List suppliers",
            description = "Returns paginated suppliers with an optional active status filter."
    )
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @GetMapping
    public ResponseEntity<Page<SupplierResponse>> findAll(
                                                        @RequestParam(defaultValue = "0")
                                                        @PositiveOrZero(message = "Number page cannot be negative")
                                                        Integer page,
                                                        @RequestParam(defaultValue = "20")
                                                        @Positive(message = "Size page must be positive")
                                                        @Max(value = 100, message = "Size cannot be greater than 100")
                                                        Integer size,
                                                        @RequestParam(required = false) Boolean active,
                                                        @Parameter(
                                                                description = "Field used for sorting",
                                                                schema = @Schema(allowableValues = {"id", "name", "email", "active"})
                                                        )
                                                        @RequestParam(defaultValue = "id") String sortBy,
                                                        @Parameter(
                                                                description = "Sort direction",
                                                                schema = @Schema(allowableValues = {"asc", "desc"})
                                                        )
                                                        @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, SortUtils.build(sortBy, direction, ALLOWED_SORT_FIELDS));
        return ResponseEntity.ok(service.findAll(active, pageable));
    }


    @Operation(summary = "Update a supplier")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(
                                                @PathVariable
                                                @Positive(message = "Id must be positive")
                                                Long id,
                                                @RequestBody @Valid SupplierRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }



    @Operation(
            summary = "Update supplier status",
            description = "Activates or deactivates a supplier."
    )
    @PatchMapping("/{id}/status")
    public ResponseEntity<SupplierResponse> updateStatus(
            @RequestParam
            boolean active,
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id) {
        return ResponseEntity.ok(service.updateStatus(id, active));
    }

    @Operation(summary = "Delete a supplier")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable
                                       @Positive(message = "Id must be positive")
                                       Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
