package com.store.microservices.order_service;


import com.store.microservices.order_service.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testcontainers.shaded.org.hamcrest.Matchers.is;
import static org.testcontainers.shaded.org.hamcrest.Matchers.notNullValue;

@Import(TestcontainersConfiguration.class)
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

		String items="[{\"skuCode\":\"iphone_15\",\"quantity\":2,\"location\":\"DECREMENT\"},{\"skuCode\":\"pixel_8\",\"quantity\":2,\"location\":\"DECREMENT\"}]";

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




}
