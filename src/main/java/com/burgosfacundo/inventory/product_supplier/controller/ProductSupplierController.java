package com.burgosfacundo.inventory.product_supplier.controller;

import com.burgosfacundo.inventory.common.web.SortUtils;
import com.burgosfacundo.inventory.product_supplier.dto.ProductSupplierRequest;
import com.burgosfacundo.inventory.product_supplier.dto.ProductSupplierResponse;
import com.burgosfacundo.inventory.product_supplier.service.ProductSupplierService;
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
@RequestMapping("/product-suppliers")
@RequiredArgsConstructor
public class ProductSupplierController {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "product.id",
            "supplier.id"
    );

    private final ProductSupplierService service;

    @PostMapping
    public ResponseEntity<ProductSupplierResponse> create(
            @RequestBody
            @Valid
            ProductSupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductSupplierResponse> read(
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProductSupplierResponse>> findAll(
            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "Number page cannot be negative")
            Integer page,
            @RequestParam(defaultValue = "20")
            @Positive(message = "Size page must be positive")
            @Max(value = 100, message = "Size cannot be greater than 100")
            Integer size,

            @RequestParam(required = false)
            @Positive(message = "Product id must be positive")
            Long productId,

            @RequestParam(required = false)
            @Positive(message = "Supplier id must be positive")
            Long supplierId,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                SortUtils.build(
                        sortBy,
                        direction,
                        ALLOWED_SORT_FIELDS));
        return ResponseEntity.ok(service.findAll(productId,supplierId,pageable));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable
            @Positive(message = "Id must be positive")
            Long id
    ) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}
