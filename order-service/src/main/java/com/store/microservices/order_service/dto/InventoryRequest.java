package com.store.microservices.order_service.dto;

/**
 * Record for InventoryRequest.
 * Represents a request to update or query inventory.
 */
public record InventoryRequest(
        String skuCode, // SKU code of the product
        Integer quantity // Quantity of the product
) {}
