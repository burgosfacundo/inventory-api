package com.burgosfacundo.inventory.api;

import com.burgosfacundo.inventory.config.IntegrationTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class InfrastructureApiIT extends IntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @Test
    void shouldExposeOpenApiDocumentation() {
        Map<String, Object> paths = given()
                .when()
                .get("/v3/api-docs")
                .then()
                .statusCode(200)
                .extract()
                .path("paths");

        assertThat(paths)
                .containsKeys(
                        "/api/v1/categories",
                        "/api/v1/products",
                        "/api/v1/suppliers",
                        "/api/v1/product-suppliers",
                        "/api/v1/warehouses",
                        "/api/v1/stocks",
                        "/api/v1/inventory-movements",
                        "/api/v1/stock-transfers"
                );
    }

    @Test
    void shouldExposeSwaggerUi() {
        given()
                .when()
                .get("/swagger-ui.html")
                .then()
                .statusCode(200);
    }

    @Test
    void shouldExposeHealthEndpoint() {
        given()
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void shouldExposeApplicationInfo() {
        given()
                .when()
                .get("/actuator/info")
                .then()
                .statusCode(200)
                .body("app.name", equalTo("Inventory API"))
                .body("app.description", equalTo("REST API for inventory, warehouse and stock management."));
    }

    @Test
    void shouldReturnNotFoundForUnknownResource() {
        given()
                .when()
                .get("/resource-that-does-not-exist")
                .then()
                .statusCode(404)
                .body("errorCode", equalTo("RESOURCE_NOT_FOUND"));
    }
}