package com.burgosfacundo.inventory.product.controller;

import com.burgosfacundo.inventory.common.web.SortUtils;
import com.burgosfacundo.inventory.product.dto.ProductRequest;
import com.burgosfacundo.inventory.product.dto.ProductResponse;
import com.burgosfacundo.inventory.product.service.ProductService;
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
@RequestMapping("/products")
@Tag(name = "Products", description = "Manage products, catalog data and activation status.")
@RequiredArgsConstructor
public class ProductController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "sku",
            "name",
            "salePrice",
            "active"
    );

    private final ProductService service;

    @Operation(summary = "Create a product")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody @Valid ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(request));
    }

    @Operation(summary = "Get product by id")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(
                                                    @PathVariable
                                                    @Positive(message = "Id must be positive") Long id) {
        return ResponseEntity.ok((service.findById(id)));
    }

    @Operation(
            summary = "List products",
            description = "Returns paginated products with optional category and active status filters."
    )
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findAll(

                                                        @RequestParam(defaultValue = "0")
                                                        @PositiveOrZero(message = "Number page cannot be negative")
                                                        Integer page,
                                                        @RequestParam(defaultValue = "20")
                                                        @Positive(message = "Size page must be positive")
                                                        @Max(value = 100, message = "Size cannot be greater than 100")
                                                        Integer size,
                                                        @RequestParam(required = false) Boolean active,
                                                        @RequestParam(required = false)
                                                        @Positive(message = "Category id must be positive")
                                                        Long categoryId,
                                                        @Parameter(
                                                                description = "Field used for sorting",
                                                                schema = @Schema(allowableValues = {"id", "sku", "name", "salePrice", "active"})
                                                        )
                                                        @RequestParam(defaultValue = "id") String sortBy,
                                                        @Parameter(
                                                                description = "Sort direction",
                                                                schema = @Schema(allowableValues = {"asc", "desc"})
                                                        )
                                                        @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, SortUtils.build(sortBy, direction, ALLOWED_SORT_FIELDS));
        return ResponseEntity.ok(service.findAll(categoryId, active, pageable));
    }

    @Operation(summary = "Update a product")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
                                                    @PathVariable
                                                    @Positive(message = "Id must be positive")
                                                    Long id,
                                                    @RequestBody @Valid ProductRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(
            summary = "Update product status",
            description = "Activates or deactivates a product."
    )
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductResponse> updateStatus(
                                                        @RequestParam
                                                        boolean active,
                                                        @PathVariable
                                                        @Positive(message = "Id must be positive")
                                                        Long id) {
        return ResponseEntity.ok(service.updateStatus(id, active));
    }

    @Operation(summary = "Delete a product")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable
                                                      @Positive(message = "Id must be positive")
                                                      Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
