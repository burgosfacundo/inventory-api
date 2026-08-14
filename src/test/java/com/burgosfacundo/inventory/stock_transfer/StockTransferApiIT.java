package com.burgosfacundo.inventory.stock_transfer;

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
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class StockTransferApiIT extends ApiIntegrationTest {

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

    private Integer createWarehouse(String code) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "code": "%s",
                          "name": "Warehouse %s",
                          "address": {
                            "street": "Av. Independencia",
                            "number": "1234",
                            "postalCode": "7600",
                            "city": "Mar del Plata",
                            "province": "Buenos Aires",
                            "countryCode": "AR"
                          }
                        }
                        """.formatted(code, code))
                .when()
                .post("/warehouses")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private void createMovement(Integer productId, Integer warehouseId, int quantity) {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "productId": %d,
                          "warehouseId": %d,
                          "type": "%s",
                          "quantity": %d
                        }
                        """.formatted(productId, warehouseId, "IN", quantity))
                .when()
                .post("/inventory-movements")
                .then()
                .statusCode(201);
    }

    @Test
    void shouldTransferStockBetweenWarehousesAndCreateHistory() {
        Integer categoryId = createCategory();
        Integer productId = createProduct(categoryId);

        Integer sourceWarehouseId = createWarehouse("WH-001");
        Integer destinationWarehouseId = createWarehouse("WH-002");

        createMovement(productId, sourceWarehouseId, 20);

        Integer transferId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "productId": %d,
                          "sourceWarehouseId": %d,
                          "destinationWarehouseId": %d,
                          "quantity": 8
                        }
                        """.formatted(productId, sourceWarehouseId, destinationWarehouseId))
                .when()
                .post("/stock-transfers")
                .then()
                .statusCode(201)
                .body("product.id", equalTo(productId))
                .body("sourceWarehouse.id", equalTo(sourceWarehouseId))
                .body("destinationWarehouse.id", equalTo(destinationWarehouseId))
                .body("quantity", equalTo(8))
                .extract()
                .path("id");

        given()
                .queryParam("productId", productId)
                .queryParam("warehouseId", sourceWarehouseId)
                .when()
                .get("/stocks")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].quantity", equalTo(12));

        given()
                .queryParam("productId", productId)
                .queryParam("warehouseId", destinationWarehouseId)
                .when()
                .get("/stocks")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].quantity", equalTo(8))
                .body("content[0].minimumStock", equalTo(0));

        given()
                .when()
                .get("/stock-transfers/{id}", transferId)
                .then()
                .statusCode(200)
                .body("id", equalTo(transferId))
                .body("product.id", equalTo(productId))
                .body("sourceWarehouse.id", equalTo(sourceWarehouseId))
                .body("destinationWarehouse.id", equalTo(destinationWarehouseId))
                .body("quantity", equalTo(8));

        given()
                .queryParam("productId", productId)
                .queryParam("warehouseId", sourceWarehouseId)
                .when()
                .get("/inventory-movements")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(2))
                .body("content.type", hasItems("IN", "OUT"))
                .body("content.quantity", hasItems(20, 8));

        given()
                .queryParam("productId", productId)
                .queryParam("warehouseId", destinationWarehouseId)
                .when()
                .get("/inventory-movements")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].type", equalTo("IN"))
                .body("content[0].quantity", equalTo(8));
    }


    @Test
    void shouldRejectTransferWhenStockIsInsufficientWithoutPartialChanges() {
        Integer categoryId = createCategory();
        Integer productId = createProduct(categoryId);

        Integer sourceWarehouseId = createWarehouse("WH-001");
        Integer destinationWarehouseId = createWarehouse("WH-002");

        createMovement(productId, sourceWarehouseId, 5);

        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "productId": %d,
                      "sourceWarehouseId": %d,
                      "destinationWarehouseId": %d,
                      "quantity": 8
                    }
                    """.formatted(productId, sourceWarehouseId, destinationWarehouseId))
                .when()
                .post("/stock-transfers")
                .then()
                .statusCode(409)
                .body("errorCode", equalTo("INSUFFICIENT_STOCK"));

        given()
                .queryParam("productId", productId)
                .queryParam("warehouseId", sourceWarehouseId)
                .when()
                .get("/stocks")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].quantity", equalTo(5));

        given()
                .queryParam("productId", productId)
                .queryParam("warehouseId", destinationWarehouseId)
                .when()
                .get("/stocks")
                .then()
                .statusCode(200)
                .body("content", empty());

        given()
                .queryParam("productId", productId)
                .when()
                .get("/stock-transfers")
                .then()
                .statusCode(200)
                .body("content", empty());

        given()
                .queryParam("productId", productId)
                .queryParam("warehouseId", sourceWarehouseId)
                .when()
                .get("/inventory-movements")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].type", equalTo("IN"))
                .body("content[0].quantity", equalTo(5));

        given()
                .queryParam("productId", productId)
                .queryParam("warehouseId", destinationWarehouseId)
                .when()
                .get("/inventory-movements")
                .then()
                .statusCode(200)
                .body("content", empty());
    }

}
