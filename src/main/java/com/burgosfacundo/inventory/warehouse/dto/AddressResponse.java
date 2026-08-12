package com.burgosfacundo.inventory.warehouse.dto;

import java.math.BigDecimal;

public record AddressResponse(
        String street,
        String number,
        String postalCode,
        String city,
        String province,
        String countryCode,
        BigDecimal latitude,
        BigDecimal longitude
) {}