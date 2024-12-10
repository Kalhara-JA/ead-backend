package com.store.microservices.order_service.client;

import com.store.microservices.order_service.dto.InventoryRequest;
import com.store.microservices.order_service.dto.InventoryResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * Client interface for communicating with the Inventory Service.
 * Provides methods for checking and updating inventory stock.
 */
public interface InventoryClient {

    Logger log = LoggerFactory.getLogger(InventoryClient.class);

    /**
     * Checks if a product is in stock.
     *
     * @param skuCode  the SKU code of the product
     * @param quantity the required quantity
     * @return true if the product is in stock, false otherwise
     */
    @GetExchange("/api/inventory")
    boolean isInStock(@RequestParam String skuCode, @RequestParam Integer quantity);

    /**
     * Decrements stock for a list of inventory items.
     * Uses Resilience4j Circuit Breaker and Retry for fault tolerance.
     *
     * @param inventoryRequests the list of inventory requests
     * @return the response from the inventory service
     */
    @PostExchange("api/v1/inventory/check-stock")
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    InventoryResponse decrementStock(@RequestBody List<InventoryRequest> inventoryRequests);

    /**
     * Increments stock for a list of inventory items.
     * Uses Resilience4j Circuit Breaker and Retry for fault tolerance.
     *
     * @param inventoryRequests the list of inventory requests
     * @return the response from the inventory service
     */
    @PostExchange("api/v1/inventory/increment-stock")
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    InventoryResponse incrementStock(@RequestBody List<InventoryRequest> inventoryRequests);

    /**
     * Fallback method for handling failures in inventory service communication.
     *
     * @param inventoryRequests the list of inventory requests
     * @param throwable         the exception that triggered the fallback
     * @return a default response indicating the operation could not be completed
     */
    default boolean fallbackMethod(List<InventoryRequest> inventoryRequests, Throwable throwable) {
        log.info("Cannot get inventory, failure reason: {}", throwable.getMessage());
        return false;
    }
}
