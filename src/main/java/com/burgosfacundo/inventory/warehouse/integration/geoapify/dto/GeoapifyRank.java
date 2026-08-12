package com.burgosfacundo.inventory.warehouse.integration.geoapify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoapifyRank(
        BigDecimal confidence
) {
}