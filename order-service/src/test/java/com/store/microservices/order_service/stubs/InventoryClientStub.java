package com.store.microservices.order_service.stubs;

import com.store.microservices.order_service.dto.InventoryRequest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


public  class InventoryClientStub {
    public static void stubInventoryCall(String object) {

        stubFor(post(urlEqualTo("/api/v1/inventory/check-stock"))
                .withRequestBody(equalToJson(object))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{  \"inStock\": true }")));
    }
    public static void stubInventoryCallFail(String object) {

        stubFor(post(urlEqualTo("/api/v1/inventory/check-stock"))
                .withRequestBody(equalToJson(object))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{  \"inStock\": false }")));
    }
}
