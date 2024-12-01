package com.store.microservices.product;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");
    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    static {
        mongoDBContainer.start();
    }

    @Test
    void shouldCreateProduct() {
        String requestBody = """
                {
                    "name": "Product 1",
                    "skuCode": "product_1",
                    "description": "Description 1",
                    "price": 100
                    "category": "Category 1",
                    "brand": "Brand 1",
                    "image": "Image 1"
                    "updatedAt": "2021-09-01"
                }
                """;

        var response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/products");

        System.out.println("Response: " + response.asString());

        response.then()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo("Product 1"))
                .body("skuCode", Matchers.equalTo("product_1"))
                .body("description", Matchers.equalTo("Description 1"))
                .body("price", Matchers.equalTo(100))
                .body("category", Matchers.equalTo("Category 1"))
                .body("brand", Matchers.equalTo("Brand 1"))
                .body("image", Matchers.equalTo("Image 1"))
                .body("updatedAt", Matchers.equalTo("2021-09-01"));
    }

}
