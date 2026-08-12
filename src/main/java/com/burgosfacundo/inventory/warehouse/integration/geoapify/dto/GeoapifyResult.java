package com.burgosfacundo.inventory.warehouse.integration.geoapify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoapifyResult(
        String street,
        String housenumber,
        String postcode,
        String city,
        String state,

        @JsonProperty("country_code")
        String countryCode,

        BigDecimal lat,
        BigDecimal lon,
        GeoapifyRank rank
) {
}