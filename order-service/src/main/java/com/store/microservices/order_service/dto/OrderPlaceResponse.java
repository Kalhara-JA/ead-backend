package com.store.microservices.order_service.dto;

import java.math.BigDecimal;

/**
 * Record for OrderPlaceResponse.
 * Represents the response after placing an order.
 */
public record OrderPlaceResponse(
        Long orderId,        // ID of the placed order
        String orderNumber,  // Unique order number
        BigDecimal total,    // Total amount of the order
        String exceptions    // Any exceptions or error messages
) {
}
