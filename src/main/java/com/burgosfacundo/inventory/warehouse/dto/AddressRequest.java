package com.burgosfacundo.inventory.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "Street is required")
        @Size(max = 150)
        String street,

        @NotBlank(message = "Street number is required")
        @Size(max = 20)
        String number,

        @NotBlank(message = "Postal code is required")
        @Size(max = 20)
        String postalCode,

        @NotBlank(message = "City is required")
        @Size(max = 100)
        String city,

        @NotBlank(message = "Province is required")
        @Size(max = 100)
        String province,

        @NotBlank(message = "Country code is required")
        @Size(min = 2, max = 2)
        String countryCode
) {}
