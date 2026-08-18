package com.burgosfacundo.inventory.warehouse.integration.demo;

import com.burgosfacundo.inventory.warehouse.dto.AddressRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DemoAddressValidatorTest {

    private final DemoAddressValidator validator = new DemoAddressValidator();

    @Test
    void shouldMapAddressWithoutExternalValidation() {
        var request = new AddressRequest(
                "San Martin",
                "2500",
                "B7600",
                "Mar del Plata",
                "Buenos Aires",
                "ar"
        );

        var address = validator.validate(request);

        assertThat(address.getStreet()).isEqualTo("San Martin");
        assertThat(address.getNumber()).isEqualTo("2500");
        assertThat(address.getPostalCode()).isEqualTo("B7600");
        assertThat(address.getCity()).isEqualTo("Mar del Plata");
        assertThat(address.getProvince()).isEqualTo("Buenos Aires");
        assertThat(address.getCountryCode()).isEqualTo("AR");
        assertThat(address.getLatitude()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(address.getLongitude()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}