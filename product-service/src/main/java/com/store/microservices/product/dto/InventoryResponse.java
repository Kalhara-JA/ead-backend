package com.store.microservices.product.dto;

import java.math.BigDecimal;

/**
 * DTO for responding with inventory details in the Product Service.
 */
public record InventoryResponse(
        String id,           // Unique identifier for the inventory item
        String name,         // Name of the inventory item
        String skuCode,      // SKU code associated with the item
        String category,     // Category of the item
        String brand,        // Brand of the item
        String description,  // Description of the item
        String image,        // Image URL or path for the item
        BigDecimal price,    // Price of the item
        String updatedAt,    // Last updated timestamp
        Integer quantity     // Available quantity in stock
) {
}
