package com.burgosfacundo.inventory.supplier.dto;

public record SupplierResponse(Long id,
                               String name,
                               String email,
                               String phone,
                               String description,
                               boolean active) {
}
