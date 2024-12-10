package com.store.microservices.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for order-related requests.
 * Encapsulates the details required to process an order.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String skuCode; // SKU code of the product being ordered
    private Integer quantity; // Quantity of the product being ordered
}
