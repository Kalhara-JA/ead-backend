package com.store.microservices.order_service.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class InventoryClientStub {

    public static void stubInventorySuccess(String requestBody) {
        stubFor(post(urlEqualTo("/api/v1/inventory/check-stock"))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"inStock\": true}")));
        System.out.println("Stubbed inventory success for items: " + requestBody);
    }

    public static void stubInventoryFailure(String requestBody) {
        stubFor(post(urlEqualTo("/api/v1/inventory/check-stock"))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"inStock\": false}")));
        System.out.println("Stubbed inventory failure for items: " + requestBody);
    }

    public static void stubInventoryTimeout(String requestBody, int delay) {
        stubFor(post(urlEqualTo("/api/v1/inventory/check-stock"))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withStatus(504)
                        .withFixedDelay(delay)));
        System.out.println("Stubbed inventory timeout for items: " + requestBody);
    }
}