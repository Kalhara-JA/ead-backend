package com.store.microservices.order_service;

import com.store.microservices.order_service.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Import(TestcontainersConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceApplicationTests {

	@LocalServerPort
	private Integer port;

	static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0.3");

	static {
		mySQLContainer.start();
	}

	@BeforeEach
	void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	private String createOrderRequest(String email) {
		return String.format("""
                {
                   "items": [
                     {
                       "skuCode": "iphone_15",
                       "quantity": 2
                     },
                     {
                       "skuCode": "pixel_8",
                       "quantity": 2
                     }
                   ],
                   "total": 2500,
                   "shippingAddress": "69/2, Wellappiligewatta, Bellpamulla, Deiyanadara",
                   "date": "2024-11-29",
                   "userDetails": {
                     "email": "%s",
                     "firstName": "Kavindu",
                     "lastName": "Dilshan"
                   }
                }
                """, email);
	}

	@Test
	@Order(1)
	void shouldCreateOrder() {
		String requestBody = createOrderRequest("kavidil20010331@gmail.com");
		String inventoryItems = "[{\"skuCode\":\"iphone_15\",\"quantity\":2},{\"skuCode\":\"pixel_8\",\"quantity\":2}]";

		InventoryClientStub.stubInventorySuccess(inventoryItems);

		var responseBody = RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/v1/orders")
				.then()
				.statusCode(201)
				.extract()
				.body().asString();

		JsonPath jsonPath = new JsonPath(responseBody);
		Long orderId = jsonPath.getLong("orderId");
		assertThat(orderId, notNullValue());
	}

	@Test
	@Order(2)
	void shouldGetAllOrders() {
		shouldCreateOrder();
		var responseBody = RestAssured.given()
				.when()
				.get("/api/v1/orders")
				.then()
				.statusCode(200)
				.extract()
				.body().asString();

		JsonPath jsonPath = new JsonPath(responseBody);
		assertThat(jsonPath.getList("$"), hasSize(greaterThanOrEqualTo(1)));
	}

	@Test
	@Order(3)
	void shouldGetMyOrders() {
		String requestBody = createOrderRequest("kavidil20010331@gmail.com");
		String inventoryItems = "[{\"skuCode\":\"iphone_15\",\"quantity\":2},{\"skuCode\":\"pixel_8\",\"quantity\":2}]";

		InventoryClientStub.stubInventorySuccess(inventoryItems);

		RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/v1/orders")
				.then()
				.statusCode(201);

		var responseBody = RestAssured.given()
				.when()
				.get("/api/v1/orders/user/kavidil20010331@gmail.com/orders")
				.then()
				.statusCode(200)
				.extract()
				.body().asString();

		JsonPath jsonPath = new JsonPath(responseBody);
		assertThat(jsonPath.getList("$"), hasSize(greaterThanOrEqualTo(1)));
	}

	@Test
	@Order(4)
	void shouldPayForOrder() {
		var responseBody = RestAssured.given()
				.when()
				.put("/api/v1/orders/1/payment")
				.then()
				.statusCode(200)
				.extract()
				.body().asString();

		assertThat(responseBody, is("Payment successfully done"));
	}

	@Test
	@Order(5)
	void shouldShipOrder() {
		var responseBody = RestAssured.given()
				.when()
				.put("/api/v1/orders/1/ship")
				.then()
				.statusCode(200)
				.extract()
				.body().asString();

		assertThat(responseBody, is("Order shipped successfully"));
	}

	@Test
	@Order(6)
	void shouldDeliverOrder() {
		var responseBody = RestAssured.given()
				.when()
				.put("/api/v1/orders/1/deliver")
				.then()
				.statusCode(200)
				.extract()
				.body().asString();

		assertThat(responseBody, is("Order delivered successfully"));
	}

	@Test
	@Order(6)
	void shouldGetOrderByOrderNumber() {
		String requestBody = createOrderRequest("kavidil20010331@gmail.com");
		String inventoryItems = "[{\"skuCode\":\"iphone_15\",\"quantity\":2},{\"skuCode\":\"pixel_8\",\"quantity\":2}]";

		InventoryClientStub.stubInventorySuccess(inventoryItems);

		var createResponseBody = RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/v1/orders")
				.then()
				.statusCode(201)
				.extract()
				.body().asString();

		JsonPath jsonPath = new JsonPath(createResponseBody);
		String orderNumber = jsonPath.getString("orderNumber");

		var responseBody = RestAssured.given()
				.when()
				.get("/api/v1/orders/" + orderNumber)
				.then()
				.statusCode(200)
				.extract()
				.body().asString();

		assertThat(responseBody, notNullValue());
	}
}