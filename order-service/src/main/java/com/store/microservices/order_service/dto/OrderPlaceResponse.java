package com.store.microservices.order_service.dto;

import java.math.BigDecimal;

public record OrderPlaceResponse(
        Long orderId,
        String orderNumber,
        BigDecimal total,
        String exceptions
) {
}
