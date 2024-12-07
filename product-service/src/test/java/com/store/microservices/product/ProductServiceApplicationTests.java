package com.store.microservices.product;

import com.store.microservices.product.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
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
                    "category": "Category 1",
                    "brand": "Brand 1",
                    "description": "Description 1",
                    "image": "Image 1",
                    "price": 100,
                    "updatedAt": "2021-09-01"
                }
                """;
        InventoryClientStub.stubInventoryCall("product_1");
        var response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/v1/products");

        System.out.println("Response: " + response.asString());

        response.then()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo("Product 1"))
                .body("skuCode", Matchers.equalTo("product_1"))
                .body("category", Matchers.equalTo("Category 1"))
                .body("brand", Matchers.equalTo("Brand 1"))
                .body("description", Matchers.equalTo("Description 1"))
                .body("image", Matchers.equalTo("Image 1"))
                .body("price", Matchers.equalTo(100))
                .body("updatedAt", Matchers.equalTo("2021-09-01"));
    }
    
    @Test
    void shouldCreateCategory() {
        String requestBody = """
                {
                    "name": "Category 1",
                    "skuCode": "Category_1"
                }
                """;

        var response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/v1/products/createCategory");

        System.out.println("Response: " + response.asString());

        response.then()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo("Category 1"))
                .body("skuCode", Matchers.equalTo("Category_1"));
    }

    @Test
    void shouldReturnAllProducts() {
        var response = RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/v1/products");

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200);

        if (!response.jsonPath().getList("$").isEmpty()) {
            response.then()
                    .body("[0].id", Matchers.notNullValue())
                    .body("[0].name", Matchers.notNullValue())
                    .body("[0].skuCode", Matchers.notNullValue())
                    .body("[0].category", Matchers.notNullValue())
                    .body("[0].brand", Matchers.notNullValue())
                    .body("[0].description", Matchers.notNullValue())
                    .body("[0].image", Matchers.notNullValue())
                    .body("[0].price", Matchers.greaterThanOrEqualTo(0));
        }
    }

    @Test
    void shouldReturnAllCategories() {
        var response = RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/v1/products/getAllCategories");

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200);

        if (!response.jsonPath().getList("$").isEmpty()) {
            response.then()
                    .body("[0].id", Matchers.notNullValue())
                    .body("[0].name", Matchers.notNullValue())
                    .body("[0].skuCode", Matchers.notNullValue());
        }
    }

//    @Test
//    void shouldReturnProductById() {
//        // Given
//        String productId = "674b3218824cd7236b691b59";
//
//        // When
//        var response = RestAssured.given()
//                .contentType("application/json")
//                .when()
//                .get("/api/v1/products/" + productId);
//
//        // Log the response
//        System.out.println("Response: " + response.asString());
//
//        // Then
//        response.then()
//                .statusCode(200)
//                .body("id", Matchers.equalTo(productId))
//                .body("name", Matchers.equalTo("iphone 13"))
//                .body("skuCode", Matchers.equalTo("iphone_13"))
//                .body("category", Matchers.equalTo("Smart Phone"))
//                .body("brand", Matchers.equalTo("Apple"))
//                .body("description", Matchers.equalTo("From Apple"))
//                .body("image", Matchers.equalTo("https://loop-mobile.com/cdn/shop/files/iphone13_pro_max_black_both_4bbedbf7-653c-48a4-ae19-23b11465d1a8.jpg?v=1724362067"))
//                .body("price", Matchers.equalTo(350));
////                .body("updatedAt", Matchers.equalTo("")); // Validate null value
//    }


    @Test
    void shouldDeleteProductById() {
        // Given
        String productId = "67424340412b017af037c032";

        // When
        var response = RestAssured.given()
                .contentType("application/json")
                .when()
                .delete("/api/v1/products/" + productId);

        // Log the response
        System.out.println("Response: " + response.asString());

        // Then
        response.then()
                .statusCode(200) // Assuming successful deletion returns HTTP 200
                .body(Matchers.equalTo("Product Deleted")); // Verify the response message
    }

//    @Test
//    void shouldUpdateProductById() {
//        // Given
//        String productId = "673eacc8dbac764f6fb72695";
//        String requestBody = """
//        {
//            "name": "MacBook pro (M4)",
//            "skuCode": "macbook_pro_m4",
//            "category": "Tablet",
//            "brand": "Apple",
//            "description": "From Apple",
//            "image": "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRifbOXtII4Jx-vDwkOPb7unfRj0XjmgKQISA&s",
//            "price": 1800,
//            "updatedAt": "2024-02-25"
//        }
//        """;
//
//        // When
//        var response = RestAssured.given()
//                .contentType("application/json")
//                .body(requestBody)
//                .when()
//                .put("/api/v1/products/" + productId);
//
//        // Log the response
//        System.out.println("Response: " + response.asString());
//
//        // Then
//        response.then()
//                .statusCode(200) // Ensure the response status is 200 (OK)
//                .body("id", Matchers.equalTo(productId)) // Ensure the ID matches the request
//                .body("name", Matchers.equalTo("MacBook pro (M4)")) // Ensure the name is updated
//                .body("skuCode", Matchers.equalTo("macbook_pro_m4")) // Ensure the SKU code is updated
//                .body("category", Matchers.equalTo("Tablet")) // Ensure the category is updated
//                .body("brand", Matchers.equalTo("Apple")) // Ensure the brand is updated
//                .body("description", Matchers.equalTo("From Apple")) // Ensure the description is updated
//                .body("image", Matchers.equalTo("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRifbOXtII4Jx-vDwkOPb7unfRj0XjmgKQISA&s")) // Ensure the image URL is updated
//                .body("price", Matchers.equalTo(1800)) // Ensure the price is updated
//                .body("updatedAt", Matchers.equalTo("2024-02-25")); // Ensure the updatedAt field is updated
//    }






}
