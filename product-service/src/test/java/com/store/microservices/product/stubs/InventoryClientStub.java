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

    public static void stubInventoryQuantityCall() {
        stubFor(get(urlEqualTo("/api/v1/inventory"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {
                                        "id": 1,
                                        "skuCode": "product_1",
                                        "quantity": 10,
                                        "location": "A1",
                                        "status": "AVAILABLE"
                                    },
                                    {
                                        "id": 2,
                                        "skuCode": "product_2",
                                        "quantity": 5,
                                        "location": "B2",
                                        "status": "AVAILABLE"
                                    }
                                ]
                                """)));
    }

    public  static void stubInventoryDeleteCall(String skuCode) {
        stubFor(delete(urlEqualTo("/api/v1/inventory/products"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("true")));

    }

}
