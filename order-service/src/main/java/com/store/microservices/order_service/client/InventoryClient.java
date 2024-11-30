package com.store.microservices.order_service.client;

import com.store.microservices.order_service.dto.InventoryRequest;
import com.store.microservices.order_service.dto.InventoryResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface InventoryClient {
    @GetExchange("/api/inventory")
    boolean isInStock(@RequestParam String skuCode,@RequestParam Integer quantity);

  Logger log = LoggerFactory.getLogger(InventoryClient.class);

    @PostExchange("api/v1/inventory/check-stock")
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    InventoryResponse decrementStock(@RequestBody List<InventoryRequest> inventoryRequests);

    @PostExchange("api/v1/inventory/increment-stock")
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    InventoryResponse incrementStock(@RequestBody List<InventoryRequest> inventoryRequests);


    default boolean fallbackMethod(List<InventoryRequest> inventoryRequests, Throwable throwable) {
        log.info("Cannot get inventory , failure reason: {}", throwable.getMessage());
        return false;
    }



}
