package com.store.microservices.product.dto;

import java.math.BigDecimal;

public record InventoryResponse(
        String id,
        String name,
        String skuCode,
        String category,
        String brand,
        String description,
        String image,
        BigDecimal price,

        String updatedAt,
        Integer quantity) {

}

