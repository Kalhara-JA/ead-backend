package com.store.microservices.product.dto;

/**
 * DTO for creating or updating a category in the Product Service.
 */
public record CategoryRequest(
        String id,       // Unique identifier for the category
        String skuCode,  // SKU code associated with the category
        String name      // Name of the category
) {
}
