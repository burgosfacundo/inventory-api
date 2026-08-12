package com.burgosfacundo.inventory.warehouse.model;

import com.burgosfacundo.inventory.common.exception.NameRequiredException;
import com.burgosfacundo.inventory.warehouse.exception.AddressRequiredException;
import com.burgosfacundo.inventory.warehouse.exception.WarehouseCodeRequiredException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "warehouses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Embedded
    private Address address;

    public Warehouse(
            String code,
            String name,
            Address address
    ) {
        validateCode(code);
        validateName(name);
        validateAddress(address);

        this.code = code;
        this.name = name;
        this.address = address;
    }

    public void update(
            String code,
            String name,
            Address address
    ) {
        validateCode(code);
        validateName(name);
        validateAddress(address);

        this.code = code;
        this.name = name;
        this.address = address;
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new WarehouseCodeRequiredException();
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new NameRequiredException();
        }
    }

    private static void validateAddress(Address address) {
        if (address == null) {
            throw new AddressRequiredException();
        }
    }
}
