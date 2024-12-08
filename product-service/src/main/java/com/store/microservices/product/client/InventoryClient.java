package com.store.microservices.product.client;


import com.store.microservices.product.dto.InventoryResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;


public interface InventoryClient {


    @PostExchange("/api/v1/inventory/products")
    Boolean addProductToInventory(@RequestBody String skuCode);



    @GetExchange("/api/v1/inventory/getProductQuantity/{skuCode}")
    Integer getProductQuantity(@RequestParam String skuCode);


    @GetExchange("/api/v1/inventory/all")
    List<InventoryResponse> getAllInventory();

    @DeleteExchange("/api/v1/inventory/products")
    Boolean deleteProductFromInventory(@RequestBody String skuCode);



}
