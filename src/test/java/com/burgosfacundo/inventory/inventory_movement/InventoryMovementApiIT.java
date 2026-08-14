package com.burgosfacundo.inventory.inventory_movement;

import com.burgosfacundo.inventory.config.ApiIntegrationTest;
import com.burgosfacundo.inventory.warehouse.dto.AddressRequest;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.service.AddressValidator;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class InventoryMovementApiIT extends ApiIntegrationTest {

    @MockitoBean
    private AddressValidator addressValidator;

    @BeforeEach
    void setUpAddressValidator() {
        when(addressValidator.validate(any(AddressRequest.class)))
                .thenReturn(validatedAddress());
    }

    private Address validatedAddress() {
        return new Address(
                "Av. Independencia",
                "1234",
                "7600",
                "Mar del Plata",
                "Buenos Aires",
                "AR",
                new BigDecimal("-38.0055"),
                new BigDecimal("-57.5426")
        );
    }

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

    private Integer createWarehouse() {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "code": "MDQ-01",
                          "name": "Main Warehouse",
                          "address": {
                            "street": "Av. Independencia",
                            "number": "1234",
                            "postalCode": "7600",
                            "city": "Mar del Plata",
                            "province": "Buenos Aires",
                            "countryCode": "AR"
                          }
                        }
                        """)
                .when()
                .post("/warehouses")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private Integer createMovement(Integer productId, Integer warehouseId, String type, int quantity) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "productId": %d,
                          "warehouseId": %d,
                          "type": "%s",
                          "quantity": %d
                        }
                        """.formatted(productId, warehouseId, type, quantity))
                .when()
                .post("/inventory-movements")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Test
    void shouldCreateStockWhenInventoryMovementInIsCreated() {
        Integer categoryId = createCategory();
        Integer productId = createProduct(categoryId);
        Integer warehouseId = createWarehouse();

        Integer movementId = createMovement(productId, warehouseId, "IN", 20);

        given()
                .when()
                .get("/inventory-movements/{id}", movementId)
                .then()
                .statusCode(200)
                .body("product.id", equalTo(productId))
                .body("warehouse.id", equalTo(warehouseId))
                .body("type", equalTo("IN"))
                .body("quantity", equalTo(20));

        given()
                .queryParam("productId", productId)
                .queryParam("warehouseId", warehouseId)
                .when()
                .get("/stocks")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].product.id", equalTo(productId))
                .body("content[0].warehouse.id", equalTo(warehouseId))
                .body("content[0].quantity", equalTo(20))
                .body("content[0].minimumStock", equalTo(0));
    }

    @Test
    void shouldDecreaseStockWhenInventoryMovementOutIsCreated() {
        Integer categoryId = createCategory();
        Integer productId = createProduct(categoryId);
        Integer warehouseId = createWarehouse();

        createMovement(productId, warehouseId, "IN", 20);
        createMovement(productId, warehouseId, "OUT", 7);

        given()
                .queryParam("productId", productId)
                .queryParam("warehouseId", warehouseId)
                .when()
                .get("/stocks")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].quantity", equalTo(13));
    }

    @Test
    void shouldRejectMovementOutWhenStockIsInsufficientAndKeepCurrentQuantity() {
        Integer categoryId = createCategory();
        Integer productId = createProduct(categoryId);
        Integer warehouseId = createWarehouse();

        createMovement(productId, warehouseId, "IN", 13);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "productId": %d,
                          "warehouseId": %d,
                          "type": "OUT",
                          "quantity": 20
                        }
                        """.formatted(productId, warehouseId))
                .when()
                .post("/inventory-movements")
                .then()
                .statusCode(409)
                .body("errorCode", equalTo("INSUFFICIENT_STOCK"));

        given()
                .queryParam("productId", productId)
                .queryParam("warehouseId", warehouseId)
                .when()
                .get("/stocks")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].quantity", equalTo(13));
    }
}