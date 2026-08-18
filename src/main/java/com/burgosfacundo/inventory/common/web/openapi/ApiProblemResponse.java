package com.burgosfacundo.inventory.common.web.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Schema(
        name = "ApiProblem",
        description = "Standard error response returned by the API."
)
public record ApiProblemResponse(
        @Schema(example = "Resource Not Found")
        String title,

        @Schema(example = "404")
        int status,

        @Schema(example = "Product with id 999 was not found")
        String detail,

        @Schema(example = "/api/v1/products/999")
        URI instance,

        @Schema(example = "PRODUCT_NOT_FOUND")
        String errorCode,

        @Schema(
                description = "Field validation errors. Present only when request body validation fails."
        )
        Map<String, String> fieldErrors,

        @Schema(
                description = "Validation errors. Present only for method or request parameter validation failures."
        )
        List<String> errors
) {}