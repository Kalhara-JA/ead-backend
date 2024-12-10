package com.store.microservices.product.dto;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a product in the Product Service.
 */
public record ProductRequest(
        String id,           // Unique identifier for the product
        String name,         // Name of the product
        String skuCode,      // SKU code for the product
        String category,     // Category to which the product belongs
        String brand,        // Brand of the product
        String description,  // Description of the product
        String image,        // Image URL or path for the product
        BigDecimal price,    // Price of the product
        String updatedAt     // Last updated timestamp
) {
}
