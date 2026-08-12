package com.burgosfacundo.inventory.warehouse.mapper;

import com.burgosfacundo.inventory.warehouse.dto.AddressResponse;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseRequest;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseResponse;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WarehouseMapper {

    public static Warehouse toEntity(
            WarehouseRequest request,
            Address address
    ) {
        return new Warehouse(
                request.code(),
                request.name(),
                address
        );
    }

    public static WarehouseResponse toResponse(
            Warehouse warehouse
    ) {
        Address address = warehouse.getAddress();

        AddressResponse addressResponse =
                new AddressResponse(
                        address.getStreet(),
                        address.getNumber(),
                        address.getPostalCode(),
                        address.getCity(),
                        address.getProvince(),
                        address.getCountryCode(),
                        address.getLatitude(),
                        address.getLongitude()
                );

        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                addressResponse
        );
    }
}