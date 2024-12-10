package com.store.microservices.product.dto;

/**
 * DTO for responding with category details in the Product Service.
 */
public record CategoryResponse(
        String id,       // Unique identifier for the category
        String name,     // Name of the category
        String skuCode   // SKU code associated with the category
) {
}
