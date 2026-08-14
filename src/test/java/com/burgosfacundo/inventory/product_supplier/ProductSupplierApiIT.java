package com.burgosfacundo.inventory.product_supplier;

import com.burgosfacundo.inventory.config.ApiIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class ProductSupplierApiIT extends ApiIntegrationTest {

    private Integer createCategory() {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Electronics",
                          "description": "Test category"
                        }
                        """)
                .when()
                .post("/categories")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private Integer createProduct(Integer categoryId) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "sku": "SKU-001",
                          "name": "Mechanical Keyboard",
                          "description": "Test product",
                          "salePrice": 120.00,
                          "categoryId": %d
                        }
                        """.formatted(categoryId))
                .when()
                .post("/products")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private Integer createSupplier() {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Tech Supplier",
                          "email": "supplier@test.com",
                          "phone": "2235551234",
                          "description": "Test supplier"
                        }
                        """)
                .when()
                .post("/suppliers")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private Integer createAssociation(Integer productId, Integer supplierId) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "productId": %d,
                          "supplierId": %d,
                          "purchasePrice": %s
                        }
                        """.formatted(productId, supplierId, "80.00"))
                .when()
                .post("/product-suppliers")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Test
    void shouldCreateRetrieveAndUpdateProductSupplier() {
        Integer categoryId = createCategory();
        Integer productId = createProduct(categoryId);
        Integer supplierId = createSupplier();

        Integer associationId = createAssociation(productId, supplierId);

        given()
                .when()
                .get("/product-suppliers/{id}", associationId)
                .then()
                .statusCode(200)
                .body("id", equalTo(associationId))
                .body("product.id", equalTo(productId))
                .body("supplier.id", equalTo(supplierId))
                .body("purchasePrice", equalTo(80.00f));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "purchasePrice": 95.50
                        }
                        """)
                .when()
                .put("/product-suppliers/{id}/purchase-price", associationId)
                .then()
                .statusCode(200)
                .body("purchasePrice", equalTo(95.50f));

        given()
                .when()
                .get("/product-suppliers/{id}", associationId)
                .then()
                .statusCode(200)
                .body("purchasePrice", equalTo(95.50f));
    }

    @Test
    void shouldReturnConflictWhenAssociationAlreadyExists() {
        Integer categoryId = createCategory();
        Integer productId = createProduct(categoryId);
        Integer supplierId = createSupplier();

        createAssociation(productId, supplierId);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "productId": %d,
                          "supplierId": %d,
                          "purchasePrice": 90.00
                        }
                        """.formatted(productId, supplierId))
                .when()
                .post("/product-suppliers")
                .then()
                .statusCode(409)
                .body("errorCode", equalTo("PRODUCT_SUPPLIER_ALREADY_EXISTS"));
    }

    @Test
    void shouldFilterAssociationsByProductAndSupplier() {
        Integer categoryId = createCategory();
        Integer productId = createProduct(categoryId);
        Integer supplierId = createSupplier();

        Integer associationId = createAssociation(productId, supplierId);

        given()
                .queryParam("productId", productId)
                .queryParam("supplierId", supplierId)
                .when()
                .get("/product-suppliers")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].id", equalTo(associationId))
                .body("content[0].product.id", equalTo(productId))
                .body("content[0].supplier.id", equalTo(supplierId))
                .body("content[0].purchasePrice", equalTo(80.00f));
    }

    @Test
    void shouldDeleteProductSupplierAssociation() {
        Integer categoryId = createCategory();
        Integer productId = createProduct(categoryId);
        Integer supplierId = createSupplier();

        Integer associationId = createAssociation(productId, supplierId);

        given()
                .when()
                .delete("/product-suppliers/{id}", associationId)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/product-suppliers/{id}", associationId)
                .then()
                .statusCode(404)
                .body("errorCode", equalTo("PRODUCT_SUPPLIER_NOT_FOUND"));
    }
}