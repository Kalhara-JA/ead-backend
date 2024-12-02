package com.store.microservices.order_service;


import com.store.microservices.order_service.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;



@Import(TestcontainersConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceApplicationTests {

	@ServiceConnection
	static MySQLContainer mySQLContainer = new MySQLContainer("mysql");


	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setup()
	{

		RestAssured.baseURI="http://localhost";
		RestAssured.port=port;

	}


	static {
		mySQLContainer.start();

	}


	@Order(1)
	@Test
	void shouldCreateOrder() {
		String requestBody = """
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
				   "shippingAddress": "69/2,Wellappiligewatta,Bellpamulla,Deiyanadara",
				   "date": "2024-11-29",
				   "userDetails": {
				     "email": "kavidil20010331@gmail.com",
				     "firstName": "Kavindu",
				     "lastName": "Dilshan"
				   }
				 }
					   
				""";

		String items="[{\"skuCode\":\"iphone_15\",\"quantity\":2},{\"skuCode\":\"pixel_8\",\"quantity\":2}]";

		InventoryClientStub.stubInventoryCall(items);

		var responseBody =RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/v1/orders")
				.then()
				.log().all()
				.statusCode(201)
				.extract()
				.body().asString();

		JsonPath jsonPath = new JsonPath(responseBody);
		Long orderId = jsonPath.getLong("orderId"); // Extract the order ID
		String orderNumber = jsonPath.getString("orderNumber");


		assertThat(orderId, Matchers.notNullValue()); // Ensure orderId is not null

	}

	@Order(2)
	@Test
	void shouldGetAllOrder(){
		shouldCreateOrder();
		var responseBody = RestAssured.given()
				.when()
				.get("/api/v1/orders")
				.then()
				.log().all()
				.statusCode(200)
				.extract()
				.body().asString();

		JsonPath jsonPath = new JsonPath(responseBody);
		assertThat(jsonPath.getList("$"), hasSize(2));
	}

	@Order(3)
	@Test
	void shouldGetMyOrder() {

		String requestBody = """
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
				   "shippingAddress": "69/2,Wellappiligewatta,Bellpamulla,Deiyanadara",
				   "date": "2024-11-29",
				   "userDetails": {
				     "email": "kavidil@gmail.com",
				     "firstName": "Kavindu",
				     "lastName": "Dilshan"
				   }
				 }
					   
				""";

		String items = "[{\"skuCode\":\"iphone_15\",\"quantity\":2},{\"skuCode\":\"pixel_8\",\"quantity\":2}]";

		InventoryClientStub.stubInventoryCall(items);

		RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/v1/orders")
				.then()
				.log().all()
				.statusCode(201)
				.extract()
				.body().asString();

		var responseBody = RestAssured.given()
				.when()
				.get("/api/v1/orders/user/kavidil20010331@gmail.com/orders")
				.then()
				.log().all()
				.statusCode(200)
				.extract()
				.body().asString();

		JsonPath jsonPath = new JsonPath(responseBody);
		assertThat(jsonPath.getList("$"), hasSize(2));

	}


	@Order(4)
	@Test
	void shouldPay(){
		var responseBody = RestAssured.given()
				.when()
				.put("/api/v1/orders/1/payment")
				.then()
				.log().all()
				.statusCode(200)
				.extract()
				.body().asString();

		assertThat(responseBody, Matchers.is("Payment successfully done"));
	}

	@Order(5)
	@Test
	void shouldShip(){
		var responseBody = RestAssured.given()
				.when()
				.put("/api/v1/orders/1/ship")
				.then()
				.log().all()
				.statusCode(200)
				.extract()
				.body().asString();

		assertThat(responseBody, Matchers.is("Order shipped successfully"));
	}

	@Order(6)
	@Test
	void shouldDeliver(){
		var responseBody = RestAssured.given()
				.when()
				.put("/api/v1/orders/1/ship")
				.then()
				.log().all()
				.statusCode(200)
				.extract()
				.body().asString();

		assertThat(responseBody, Matchers.is("Order shipped successfully"));
	}

	@Order(7)
	@Test
	void shouldGetOrderByOrderNumber(){
		String createRequestBody = """
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
				   "shippingAddress": "69/2,Wellappiligewatta,Bellpamulla,Deiyanadara",
				   "date": "2024-11-29",
				   "userDetails": {
				     "email": "kavidil20010331@gmail.com",
				     "firstName": "Kavindu",
				     "lastName": "Dilshan"
				   }
				 }
					   
				""";

		String items="[{\"skuCode\":\"iphone_15\",\"quantity\":2},{\"skuCode\":\"pixel_8\",\"quantity\":2}]";

		InventoryClientStub.stubInventoryCall(items);

		var createResponseBody =RestAssured.given()
				.contentType("application/json")
				.body(createRequestBody)
				.when()
				.post("/api/v1/orders")
				.then()
				.log().all()
				.statusCode(201)
				.extract()
				.body().asString();

		JsonPath jsonPath = new JsonPath(createResponseBody);
		String orderNumber = jsonPath.getString("orderNumber");

		var responseBody = RestAssured.given()
				.when()
				.get("/api/v1/orders/"+orderNumber)
				.then()
				.log().all()
				.statusCode(200)
				.extract()
				.body().asString();

		assertThat(responseBody, Matchers.notNullValue());
	}

}
