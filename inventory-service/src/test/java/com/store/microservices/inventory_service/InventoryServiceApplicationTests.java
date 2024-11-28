package com.store.microservices.inventory_service;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryServiceApplicationTests {

    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.3.0");

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    static {
        mySQLContainer.start();
    }

    @Test
    void shouldAddProductToInventory() {
        String skuCode = "iphone_15";

        var response = RestAssured.given()
                .queryParam("skuCode", skuCode)
                .when()
                .post("/api/v1/inventory/products");

        response.then()
                .statusCode(200)
                .body("skuCode", Matchers.equalTo(skuCode));
    }

    @Test
    void shouldFetchAllInventory() {
        var response = RestAssured.given()
                .when()
                .get("/api/v1/inventory");

        response.then()
                .statusCode(200)
                .body("size()", Matchers.greaterThanOrEqualTo(0));
    }

    @Test
    void shouldCheckStockForProduct() {
        String skuCode = "test_product";
        int quantity = 10;

        // Assume product exists in the database with sufficient stock
        var response = RestAssured.given()
                .queryParam("skuCode", skuCode)
                .queryParam("quantity", quantity)
                .when()
                .get("/api/v1/inventory/check-stock");

        response.then()
                .statusCode(200)
                .body("skuCode", Matchers.equalTo(skuCode))
                .body("inStock", Matchers.equalTo(true));
    }

    @Test
    void shouldDeductStockFromInventory() {
        String skuCode = "test_product";
        int quantity = 5;

        // Assume product exists in the database with sufficient stock
        var response = RestAssured.given()
                .queryParam("skuCode", skuCode)
                .queryParam("quantity", quantity)
                .when()
                .post("/api/v1/inventory/deduct");

        response.then()
                .statusCode(200)
                .body("skuCode", Matchers.equalTo(skuCode))
                .body("quantity", Matchers.greaterThanOrEqualTo(0));
    }

    @Test
    void shouldRestockInventory() {
        String skuCode = "restock_test_product";
        int quantity = 20;

        // Add stock to the inventory
        var response = RestAssured.given()
                .queryParam("skuCode", skuCode)
                .queryParam("quantity", quantity)
                .when()
                .post("/api/v1/inventory/restock");

        response.then()
                .statusCode(200)
                .body("skuCode", Matchers.equalTo(skuCode))
                .body("quantity", Matchers.equalTo(quantity));
    }

    @Test
    void shouldReturnLowStockItems() {
        var response = RestAssured.given()
                .when()
                .get("/api/v1/inventory/low-stock");

        response.then()
                .statusCode(200)
                .body("size()", Matchers.greaterThanOrEqualTo(0));
    }

    @Test
    void shouldCheckAndProcessOrder() {
        String requestBody = """
                [
                    {"skuCode": "order_test_product", "quantity": 2},
                    {"skuCode": "order_test_product_2", "quantity": 5}
                ]
                """;

        var response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/v1/inventory/check-stock");

        response.then()
                .statusCode(Matchers.anyOf(Matchers.equalTo(200), Matchers.equalTo(409)))
                .body("inStock", Matchers.notNullValue());
    }
}
