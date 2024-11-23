package com.store.microservices.product.dto;

import java.math.BigDecimal;

public record CategoryResponse(
        String id,
        String skuCode,
        String name

      ) {
}

