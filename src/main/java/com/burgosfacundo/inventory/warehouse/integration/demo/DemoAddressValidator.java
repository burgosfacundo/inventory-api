package com.burgosfacundo.inventory.warehouse.integration.demo;

import com.burgosfacundo.inventory.warehouse.dto.AddressRequest;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.service.AddressValidator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;

@Component
@Profile("demo")
public class DemoAddressValidator implements AddressValidator {

    @Override
    public Address validate(AddressRequest request) {
        return new Address(
                request.street(),
                request.number(),
                request.postalCode(),
                request.city(),
                request.province(),
                request.countryCode().toUpperCase(Locale.ROOT),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}