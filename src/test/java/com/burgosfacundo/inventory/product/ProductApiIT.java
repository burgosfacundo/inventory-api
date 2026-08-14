package com.burgosfacundo.inventory.product;

import com.burgosfacundo.inventory.config.ApiIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class ProductApiIT extends ApiIntegrationTest {

    private Integer createCategory() {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "%s",
                          "description": "Test category"
                        }
                        """.formatted("Electronics"))
                .when()
                .post("/categories")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Test
    void shouldCreateAndRetrieveProduct() {
        Integer categoryId = createCategory();

        Integer productId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "sku": "SKU-001",
                          "name": "Mechanical Keyboard",
                          "description": "RGB mechanical keyboard",
                          "salePrice": 99.99,
                          "categoryId": %d
                        }
                        """.formatted(categoryId))
                .when()
                .post("/products")
                .then()
                .statusCode(201)
                .body("sku", equalTo("SKU-001"))
                .body("name", equalTo("Mechanical Keyboard"))
                .body("salePrice", equalTo(99.99f))
                .body("active", equalTo(true))
                .body("category.id", equalTo(categoryId))
                .body("category.name", equalTo("Electronics"))
                .extract()
                .path("id");

        given()
                .when()
                .get("/products/{id}", productId)
                .then()
                .statusCode(200)
                .body("id", equalTo(productId))
                .body("sku", equalTo("SKU-001"))
                .body("name", equalTo("Mechanical Keyboard"))
                .body("description", equalTo("RGB mechanical keyboard"))
                .body("active", equalTo(true))
                .body("category.id", equalTo(categoryId));
    }

    @Test
    void shouldReturnConflictWhenSkuAlreadyExists() {
        Integer categoryId = createCategory();

        String productJson = """
                {
                  "sku": "SKU-001",
                  "name": "Mechanical Keyboard",
                  "description": "RGB mechanical keyboard",
                  "salePrice": 99.99,
                  "categoryId": %d
                }
                """.formatted(categoryId);

        given()
                .contentType(ContentType.JSON)
                .body(productJson)
                .when()
                .post("/products")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(productJson)
                .when()
                .post("/products")
                .then()
                .statusCode(409)
                .body("errorCode", equalTo("PRODUCT_SKU_ALREADY_EXISTS"));
    }

    @Test
    void shouldReturnNotFoundWhenCategoryDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "sku": "SKU-001",
                          "name": "Mechanical Keyboard",
                          "description": "RGB mechanical keyboard",
                          "salePrice": 99.99,
                          "categoryId": 999
                        }
                        """)
                .when()
                .post("/products")
                .then()
                .statusCode(404)
                .body("errorCode", equalTo("CATEGORY_NOT_FOUND"));
    }

    @Test
    void shouldDeactivateAndDeleteProduct() {
        Integer categoryId = createCategory();

        Integer productId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "sku": "SKU-001",
                      "name": "Mechanical Keyboard",
                      "description": "RGB mechanical keyboard",
                      "salePrice": 99.99,
                      "categoryId": %d
                    }
                    """.formatted(categoryId))
                .when()
                .post("/products")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .queryParam("active", false)
                .when()
                .patch("/products/{id}/status", productId)
                .then()
                .statusCode(200)
                .body("active", equalTo(false));

        given()
                .when()
                .get("/products/{id}", productId)
                .then()
                .statusCode(200)
                .body("active", equalTo(false));

        given()
                .when()
                .delete("/products/{id}", productId)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/products/{id}", productId)
                .then()
                .statusCode(404)
                .body("errorCode", equalTo("PRODUCT_NOT_FOUND"));
    }
}