package com.burgosfacundo.inventory.supplier.mapper;

import com.burgosfacundo.inventory.supplier.dto.SupplierRequest;
import com.burgosfacundo.inventory.supplier.dto.SupplierResponse;
import com.burgosfacundo.inventory.supplier.model.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SupplierMapper {

    public static Supplier toEntity(SupplierRequest request){
        return new Supplier(request.name(), request.email(), request.phone(),request.description());
    }

    public static SupplierResponse toResponse(Supplier entity){
        return new SupplierResponse(entity.getId(), entity.getName(), entity.getEmail(),
                entity.getPhone(), entity.getDescription(), entity.isActive());
    }
}
