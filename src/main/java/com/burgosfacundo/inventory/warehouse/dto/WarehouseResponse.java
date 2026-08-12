package com.burgosfacundo.inventory.warehouse.dto;

public record WarehouseResponse(
        Long id,
        String code,
        String name,
        AddressResponse address
) {}