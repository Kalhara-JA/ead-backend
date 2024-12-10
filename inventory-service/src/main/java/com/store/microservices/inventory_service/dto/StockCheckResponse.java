package com.store.microservices.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for stock check responses.
 * Indicates whether a product is in stock.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockCheckResponse {
    private Boolean isInStock; // True if the product is in stock, false otherwise
}
