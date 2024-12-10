package com.store.microservices.order_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Record for OrderResponse.
 * Represents the details of an order, including items, status, and user information.
 */
public record OrderResponse(
        Long id,
        String orderNumber,
        List<OrderItem> items,
        BigDecimal total,
        LocalDate date,
        String shippingAddress,
        String paymentStatus,
        String deliveryStatus,
        String email
) {
    /**
     * Record for an item in the order response.
     */
    public record OrderItem(String skuCode, Integer quantity) {
    }
}
