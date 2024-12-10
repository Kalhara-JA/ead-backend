package com.store.microservices.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for inventory-related requests.
 * Encapsulates the details required to perform operations on inventory.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequest {
    private String skuCode; // SKU code of the inventory item
    private Integer quantity; // Quantity of the inventory item
    private String location; // Location of the inventory item
}
