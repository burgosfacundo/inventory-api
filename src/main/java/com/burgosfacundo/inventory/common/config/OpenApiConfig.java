package com.burgosfacundo.inventory.common.config;

import com.burgosfacundo.inventory.common.web.openapi.ApiProblemResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenApi(
            @Value("${api.documentation.title}") String title,
            @Value("${api.documentation.version}") String version,
            @Value("${api.documentation.description}") String description
    ) {
        Components components = new Components();

        ModelConverters.getInstance()
                .read(ApiProblemResponse.class)
                .forEach(components::addSchemas);

        components
                .addResponses("BadRequest", problemResponse("Invalid request or validation failure."))
                .addResponses("NotFound", problemResponse("Requested resource was not found."))
                .addResponses("Conflict", problemResponse("Request conflicts with the current state of the resource."))
                .addResponses("UnprocessableContent", problemResponse("Request could not be processed because of a business or external validation rule."))
                .addResponses("ServiceUnavailable", problemResponse("A required external service is temporarily unavailable."))
                .addResponses("InternalServerError", problemResponse("Unexpected internal server error."));

        return new OpenAPI()
                .components(components)
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(description));
    }

    private ApiResponse problemResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        "application/problem+json",
                        new io.swagger.v3.oas.models.media.MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/ApiProblem"))
                ));
    }
}