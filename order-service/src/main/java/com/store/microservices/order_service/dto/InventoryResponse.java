package com.store.microservices.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for InventoryResponse.
 * Represents the response indicating inventory stock status.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponse {
        private boolean inStock; // Indicates whether the product is in stock
}
