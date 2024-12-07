package com.store.microservices.inventory_service;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryServiceApplicationTests {

    @ServiceConnection
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.3.0");
    @LocalServerPort
    private Integer port;


    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    static {
        mySQLContainer.start();
    }

    private static final String WAREHOUSE_ENDPOINT = "/api/v1/inventory/warehouse";

    @Test
    void shouldReadInventory() {
        var response = RestAssured.given()
                .when()
                .get("/api/inventory?skuCode=iphone_15&quantity=1")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(Boolean.class);
        assertTrue(response);

        var negativeResponse = RestAssured.given()
                .when()
                .get("/api/inventory?skuCode=iphone_15&quantity=10000")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(Boolean.class);
        assertFalse(negativeResponse);
    }

    @Test
    void shouldGetAllWarehouses() {
        RestAssured.given()
                .when()
                .get(WAREHOUSE_ENDPOINT)
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0)); // Check at least zero results.
    }

    @Test
    void shouldCreateWarehouse() {
        final String warehouseName = "Central Warehouse";
        final String warehouseAddress = "123 Main St, Cityville";
        final String warehouseManager = "John Doe";

        // Create request payload using variables
        var warehouseRequest = """
        {
            "name": "%s",
            "address": "%s",
            "managerName": "%s"
        }
        """.formatted(warehouseName, warehouseAddress, warehouseManager);

        RestAssured.given()
                .contentType("application/json")
                .body(warehouseRequest)
                .when()
                .post(WAREHOUSE_ENDPOINT)
                .then()
                .statusCode(200)
                .body("name", equalTo(warehouseName))
                .body("address", equalTo(warehouseAddress))
                .body("managerName", equalTo(warehouseManager));
    }

    @Test
    void shouldUpdateWarehouse() {
        // Define reusable variables
        final int warehouseId = 1; // ID of the warehouse to be updated
        final String updatedName = "Updated Warehouse";
        final String updatedAddress = "456 New Rd, Townsville";
        final String updatedManagerName = "Jane Smith";

        // Create request payload using variables
        var warehouseRequest = """
        {
            "name": "%s",
            "address": "%s",
            "managerName": "%s"
        }
        """.formatted(updatedName, updatedAddress, updatedManagerName);

        // Assume the warehouse with ID 1 exists (or you may create it as a pre-step).
        RestAssured.given()
                .contentType("application/json")
                .body(warehouseRequest)
                .when()
                .put(WAREHOUSE_ENDPOINT + "/" + warehouseId)
                .then()
                .statusCode(200)
                .body("name", equalTo(updatedName))
                .body("address", equalTo(updatedAddress))
                .body("managerName", equalTo(updatedManagerName));
    }

    @Test
    void shouldDeleteWarehouse() {
        final int warehouseId = 1; // ID of the warehouse to delete
        final String expectedResponseMessage = "Warehouse with ID %d has been deleted successfully".formatted(warehouseId);
        RestAssured.given()
                .when()
                .delete(WAREHOUSE_ENDPOINT + "/" + warehouseId)
                .then()
                .statusCode(200)
                .body(equalTo(expectedResponseMessage)); // Check response message.
    }



}
