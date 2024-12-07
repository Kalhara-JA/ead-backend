package com.store.microservices.product.dto;

import java.math.BigDecimal;

public record ProductRequest(
        String id,
        String name,
        String skuCode,
        String category,
        String brand,
        String description,
        String image,
        BigDecimal price,

        String updatedAt

) {

}
