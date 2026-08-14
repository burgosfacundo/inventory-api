package com.burgosfacundo.inventory.category;

import com.burgosfacundo.inventory.category.repository.CategoryRepository;
import com.burgosfacundo.inventory.config.ApiIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;

class CategoryApiIT extends ApiIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldCreateAndRetrieveCategory() {
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Electronics",
                          "description": "Electronic devices"
                        }
                        """)
                .when()
                .post("/categories")
                .then()
                .statusCode(201)
                .body("name", equalTo("Electronics"))
                .body("description", equalTo("Electronic devices"))
                .extract()
                .path("id");

        given()
                .when()
                .get("/categories/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("name", equalTo("Electronics"))
                .body("description", equalTo("Electronic devices"));
    }

    @Test
    void shouldReturnValidationErrorWhenCategoryNameIsBlank() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "name": "",
                      "description": "Electronic devices"
                    }
                    """)
                .when()
                .post("/categories")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("errorCode", equalTo("VALIDATION_ERROR"))
                .body("fieldErrors.name", equalTo("Name is required"));

        assertThat(categoryRepository.count()).isZero();
    }
}