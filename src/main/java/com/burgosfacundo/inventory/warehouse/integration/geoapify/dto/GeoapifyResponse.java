package com.burgosfacundo.inventory.warehouse.integration.geoapify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoapifyResponse(
        List<GeoapifyResult> results
) {
}