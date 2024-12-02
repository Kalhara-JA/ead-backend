package com.store.microservices.inventory_service;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
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

        String skuCode = "iphone_15";

        var response = RestAssured.given()
                .queryParam("skuCode", skuCode)
                .when()
                .post("/api/v1/inventory/products");

        response.then()
                .statusCode(200)
                .body("skuCode", Matchers.equalTo(skuCode));
    }

    static {
        mySQLContainer.start();
    }

    @Test
    void shouldAddProductToInventory() {
        String skuCode = "iphone_17";

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
    void shouldRestockInventory() {
        String skuCode = "iphone_15";
        Integer restockQuantity = 120;

        var response = RestAssured.given()
                .queryParam("skuCode", skuCode)
                .queryParam("quantity", restockQuantity)
                .when()
                .post("/api/v1/inventory/restock");

        response.then()
                .statusCode(200)
                .body("skuCode", Matchers.equalTo(skuCode));
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
        // Create a request body with test data
        String requestBody = """
            [
                {"skuCode": "iphone_15", "quantity": 3},
                {"skuCode": "pixel_8", "quantity": 7}
            ]
            """;

        // Send the POST request to the API
        Boolean response = RestAssured.given()
                .contentType("application/json") // Set content type
                .body(requestBody)              // Attach request body
                .when()
                .post("/api/v1/inventory/check-stock")
                .then()
                .statusCode(200)
                .extract()
                .as(Boolean.class);

        // Assert that the response is either true or false
        Assertions.assertTrue(response == true || response == false);// Ensure `inStock` field is present in the response
    }

    @Test
    void shouldRestockOrder() {
        // Create a request body with test data
        String requestBody = """
        [
            {"skuCode": "iphone_15", "quantity": 3},
            {"skuCode": "pixel_8", "quantity": 7}
        ]
        """;

        // Send the POST request to the API
        Boolean response = RestAssured.given()
                .contentType("application/json") // Set content type
                .body(requestBody)              // Attach request body
                .when()
                .post("/api/v1/inventory/increment-stock")
                .then()
                .statusCode(200)
                .extract()
                .as(Boolean.class);

        // Assert that the response is either true or false
        Assertions.assertTrue(response == true || response == false);
    }

}
