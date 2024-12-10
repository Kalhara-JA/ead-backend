package com.store.microservices.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for inventory-related responses.
 * Encapsulates the details returned after inventory operations.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryResponse {
    private String skuCode; // SKU code of the inventory item
    private Boolean isInStock; // Indicates if the item is in stock
    private Integer availableQuantity; // Quantity of the item available in inventory
    private String status; // Status message related to the inventory operation
}
