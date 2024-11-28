package com.store.microservices.product.client;


import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface InventoryClient {
    @PostExchange("/api/v1/inventory/addProduct/{skuCode}")
    Boolean addProductToInventory(@RequestParam  String skuCode);



    @GetExchange("/api/v1/inventory/getProductQuantity/{skuCode}")
    Integer getProductQuantity(@RequestParam String skuCode);

}
