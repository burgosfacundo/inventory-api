package com.burgosfacundo.inventory.warehouse.model;

import com.burgosfacundo.inventory.common.exception.NameRequiredException;
import com.burgosfacundo.inventory.warehouse.exception.AddressRequiredException;
import com.burgosfacundo.inventory.warehouse.exception.WarehouseCodeRequiredException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WarehouseTest {

    private Address validAddress() {
        return new Address(
                "Av. Independencia",
                "1234",
                "7600",
                "Mar del Plata",
                "Buenos Aires",
                "AR",
                new BigDecimal("-38.0055"),
                new BigDecimal("-57.5426")
        );
    }

    @Test
    void shouldCreateWarehouseWithValidData() {
        Address address = validAddress();

        Warehouse warehouse =
                new Warehouse("WH-001", "Main Warehouse", address);

        assertThat(warehouse.getCode()).isEqualTo("WH-001");
        assertThat(warehouse.getName()).isEqualTo("Main Warehouse");
        assertThat(warehouse.getAddress()).isEqualTo(address);
    }

    @Test
    void shouldThrowExceptionWhenCodeIsNull() {
        var exception = assertThrows(
                WarehouseCodeRequiredException.class,
                () -> new Warehouse(
                        null,
                        "Main Warehouse",
                        validAddress()
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo("WAREHOUSE_CODE_REQUIRED");
    }

    @Test
    void shouldThrowExceptionWhenCodeIsBlank() {
        assertThrows(
                WarehouseCodeRequiredException.class,
                () -> new Warehouse(
                        "",
                        "Main Warehouse",
                        validAddress()
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(
                NameRequiredException.class,
                () -> new Warehouse(
                        "WH-001",
                        null,
                        validAddress()
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(
                NameRequiredException.class,
                () -> new Warehouse(
                        "WH-001",
                        "",
                        validAddress()
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenAddressIsNull() {
        var exception = assertThrows(
                AddressRequiredException.class,
                () -> new Warehouse(
                        "WH-001",
                        "Main Warehouse",
                        null
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo("ADDRESS_REQUIRED");
    }

    @Test
    void shouldUpdateWarehouseWithValidData() {
        Warehouse warehouse = new Warehouse(
                "WH-001",
                "Main Warehouse",
                validAddress()
        );

        Address newAddress = new Address(
                "Colon",
                "2000",
                "7600",
                "Mar del Plata",
                "Buenos Aires",
                "AR",
                new BigDecimal("-38.0100"),
                new BigDecimal("-57.5500")
        );

        warehouse.update(
                "WH-002",
                "Updated Warehouse",
                newAddress
        );

        assertThat(warehouse.getCode()).isEqualTo("WH-002");
        assertThat(warehouse.getName())
                .isEqualTo("Updated Warehouse");
        assertThat(warehouse.getAddress())
                .isEqualTo(newAddress);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithInvalidCode() {
        Warehouse warehouse = new Warehouse(
                "WH-001",
                "Main Warehouse",
                validAddress()
        );

        assertThrows(
                WarehouseCodeRequiredException.class,
                () -> warehouse.update(
                        "",
                        "Main Warehouse",
                        validAddress()
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithInvalidName() {
        Warehouse warehouse = new Warehouse(
                "WH-001",
                "Main Warehouse",
                validAddress()
        );

        assertThrows(
                NameRequiredException.class,
                () -> warehouse.update(
                        "WH-001",
                        "",
                        validAddress()
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithNullAddress() {
        Warehouse warehouse = new Warehouse(
                "WH-001",
                "Main Warehouse",
                validAddress()
        );

        assertThrows(
                AddressRequiredException.class,
                () -> warehouse.update(
                        "WH-001",
                        "Main Warehouse",
                        null
                )
        );
    }
}