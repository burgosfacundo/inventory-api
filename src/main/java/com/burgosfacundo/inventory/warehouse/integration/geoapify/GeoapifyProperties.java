package com.burgosfacundo.inventory.warehouse.integration.geoapify;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Validated
@ConfigurationProperties(prefix = "geoapify")
public record GeoapifyProperties(

        @NotBlank
        String baseUrl,

        @NotBlank
        String apiKey,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        BigDecimal minConfidence
) {
}