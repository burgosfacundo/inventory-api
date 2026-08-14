package com.burgosfacundo.inventory.supplier;

import com.burgosfacundo.inventory.config.ApiIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class SupplierApiIT extends ApiIntegrationTest {

    private Integer createSupplier(String email) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Tech Supplier",
                          "email": "%s",
                          "phone": "2235551234",
                          "description": "Test supplier"
                        }
                        """.formatted(email))
                .when()
                .post("/suppliers")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Test
    void shouldCreateAndRetrieveSupplier() {
        Integer supplierId = createSupplier("supplier@test.com");

        given()
                .when()
                .get("/suppliers/{id}", supplierId)
                .then()
                .statusCode(200)
                .body("id", equalTo(supplierId))
                .body("name", equalTo("Tech Supplier"))
                .body("email", equalTo("supplier@test.com"))
                .body("phone", equalTo("2235551234"))
                .body("description", equalTo("Test supplier"))
                .body("active", equalTo(true));
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() {
        createSupplier("supplier@test.com");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Another Supplier",
                          "email": "supplier@test.com",
                          "phone": "2235559999",
                          "description": "Another supplier"
                        }
                        """)
                .when()
                .post("/suppliers")
                .then()
                .statusCode(409)
                .body("errorCode", equalTo("SUPPLIER_EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void shouldDeactivateAndDeleteSupplier() {
        Integer supplierId = createSupplier("supplier@test.com");

        given()
                .queryParam("active", false)
                .when()
                .patch("/suppliers/{id}/status", supplierId)
                .then()
                .statusCode(200)
                .body("active", equalTo(false));

        given()
                .when()
                .get("/suppliers/{id}", supplierId)
                .then()
                .statusCode(200)
                .body("active", equalTo(false));

        given()
                .when()
                .delete("/suppliers/{id}", supplierId)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/suppliers/{id}", supplierId)
                .then()
                .statusCode(404)
                .body("errorCode", equalTo("SUPPLIER_NOT_FOUND"));
    }

    @Test
    void shouldListSuppliersFilteredByActiveStatus() {
        Integer activeSupplier = createSupplier("active@test.com");
        Integer inactiveSupplier = createSupplier("inactive@test.com");

        given()
                .queryParam("active", false)
                .when()
                .patch("/suppliers/{id}/status", inactiveSupplier)
                .then()
                .statusCode(200);

        given()
                .queryParam("active", true)
                .when()
                .get("/suppliers")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].id", equalTo(activeSupplier))
                .body("content[0].active", equalTo(true));
    }
}