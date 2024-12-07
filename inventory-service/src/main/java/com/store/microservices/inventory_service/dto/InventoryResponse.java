package com.store.microservices.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryResponse {
    private String skuCode;
    private Boolean isInStock;
    private Integer availableQuantity;
    private String status;
}