package com.store.microservices.inventory_service;

import io.restassured.RestAssured;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;


@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InventoryServiceApplicationTests {

    @Container
    static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.3.0");

    @LocalServerPort
    private Integer port;


    private static final String BASE_URI = "http://localhost";
    private static final String INVENTORY_ENDPOINT = "/api/v1/inventory";
    private static final String SKU_IPHONE_15 = "iphone_15";
    private static final String SKU_PIXEL_8 = "pixel_8";

    @BeforeAll
    static void startContainer() {
        mySQLContainer.start();
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @AfterAll
    static void stopContainer() {
        mySQLContainer.stop();
    }

    private void addProduct(String skuCode) {
        RestAssured.given()
                .queryParam("skuCode", skuCode)
                .when()
                .post(INVENTORY_ENDPOINT + "/products")
                .then()
                .statusCode(200)
                .body("skuCode", Matchers.equalTo(skuCode));
    }

    @Test
    void shouldAddProductToInventory() {
        String skuCode = "iphone_17";

        RestAssured.given()
                .contentType("application/json")
                .body(skuCode)
                .when()
                .post(INVENTORY_ENDPOINT + "/products")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("true")); // Ensure the response matches the Boolean return type
    }
    @Test
    void shouldDeleteProductFromInventory() {
        String skuCode = "iphone_17";

        RestAssured.given()
                .contentType("application/json")
                .body(skuCode)
                .when()
                .delete(INVENTORY_ENDPOINT + "/products")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("true")); // Ensure the response matches the Boolean return type
    }

    private static final String WAREHOUSE_ENDPOINT = "/api/v1/inventory/warehouse";

    @Test
    void shouldFetchAllInventory() {
        RestAssured.given()
                .when()
                .get(INVENTORY_ENDPOINT)
                .then()
                .statusCode(200)
                .body("size()", Matchers.greaterThanOrEqualTo(0));
    }

    @Test
    void shouldRestockInventory() {
        Integer restockQuantity = 120;

        RestAssured.given()
                .queryParam("skuCode", SKU_IPHONE_15)
                .queryParam("quantity", restockQuantity)
                .when()
                .post(INVENTORY_ENDPOINT + "/restock")
                .then()
                .statusCode(200)
                .body("skuCode", Matchers.equalTo(SKU_IPHONE_15))
                .body("availableQuantity", Matchers.greaterThanOrEqualTo(restockQuantity));
    }

    @Test
    void shouldGetAllWarehouses() {
        RestAssured.given()
                .when()
                .get(WAREHOUSE_ENDPOINT)
                .then()
                .statusCode(200)
                .body("size()", Matchers.greaterThanOrEqualTo(0)); // Check at least zero results.
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
                .body("name", Matchers.equalTo(warehouseName))
                .body("address", Matchers.equalTo(warehouseAddress))
                .body("managerName", Matchers.equalTo(warehouseManager));
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
                .body("name", Matchers.equalTo(updatedName))
                .body("address", Matchers.equalTo(updatedAddress))
                .body("managerName", Matchers.equalTo(updatedManagerName));
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
                .body(Matchers.equalTo(expectedResponseMessage)); // Check response message.
    }



    void shouldReturnLowStockItems() {
        RestAssured.given()
                .when()
                .get(INVENTORY_ENDPOINT + "/low-stock")
                .then()
                .statusCode(200)
                .body("size()", Matchers.greaterThanOrEqualTo(0));
    }

    @Test
    void shouldCheckAndProcessOrder() {
        String requestBody = """
            [
                {"skuCode": "%s", "quantity": 3},
                {"skuCode": "%s", "quantity": 7}
            ]
            """.formatted(SKU_IPHONE_15, SKU_PIXEL_8);

        Boolean inStock = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(INVENTORY_ENDPOINT + "/check-stock")
                .then()
                .statusCode(200)
                .extract()
                .as(Boolean.class);

        Assertions.assertNotNull(inStock);
    }

    @Test
    void shouldRestockOrder() {
        String requestBody = """
            [
                {"skuCode": "%s", "quantity": 3},
                {"skuCode": "%s", "quantity": 7}
            ]
            """.formatted(SKU_IPHONE_15, SKU_PIXEL_8);

        Boolean restocked = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(INVENTORY_ENDPOINT + "/increment-stock")
                .then()
                .statusCode(200)
                .extract()
                .as(Boolean.class);

        Assertions.assertNotNull(restocked);
    }
}
