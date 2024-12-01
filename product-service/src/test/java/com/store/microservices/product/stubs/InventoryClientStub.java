package com.store.microservices.product.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class InventoryClientStub {
public static void stubInventoryCall(String skuCode) {
    stubFor(post(urlEqualTo("/api/v1/inventory/products"))
            .willReturn(aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("true")));
}
}
