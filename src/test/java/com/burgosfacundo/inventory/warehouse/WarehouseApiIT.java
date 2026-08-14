package com.burgosfacundo.inventory.warehouse;

import com.burgosfacundo.inventory.config.ApiIntegrationTest;
import com.burgosfacundo.inventory.warehouse.dto.AddressRequest;
import com.burgosfacundo.inventory.warehouse.exception.AddressNotFoundException;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.repository.WarehouseRepository;
import com.burgosfacundo.inventory.warehouse.service.AddressValidator;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class WarehouseApiIT extends ApiIntegrationTest {

    @MockitoBean
    private AddressValidator addressValidator;

    @Autowired
    private WarehouseRepository warehouseRepository;

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

    private Integer createWarehouse() {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "code": "%s",
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
                        """.formatted("MDQ-01"))
                .when()
                .post("/warehouses")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Test
    void shouldCreateAndRetrieveWarehouse() {
        Integer warehouseId = createWarehouse();

        given()
                .when()
                .get("/warehouses/{id}", warehouseId)
                .then()
                .statusCode(200)
                .body("id", equalTo(warehouseId))
                .body("code", equalTo("MDQ-01"))
                .body("name", equalTo("Main Warehouse"))
                .body("address.street", equalTo("Av. Independencia"))
                .body("address.number", equalTo("1234"))
                .body("address.city", equalTo("Mar del Plata"))
                .body("address.province", equalTo("Buenos Aires"))
                .body("address.countryCode", equalTo("AR"));
    }

    @Test
    void shouldReturnConflictWhenWarehouseCodeAlreadyExists() {
        createWarehouse();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "code": "MDQ-01",
                          "name": "Another Warehouse",
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
                .statusCode(409)
                .body("errorCode", equalTo("WAREHOUSE_CODE_ALREADY_EXISTS"));

        assertThat(warehouseRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldNotPersistWarehouseWhenAddressCannotBeValidated() {
        when(addressValidator.validate(any(AddressRequest.class)))
                .thenThrow(new AddressNotFoundException());

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "code": "MDQ-01",
                          "name": "Main Warehouse",
                          "address": {
                            "street": "Invalid street",
                            "number": "999",
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
                .statusCode(422);

        assertThat(warehouseRepository.count()).isZero();
    }
}