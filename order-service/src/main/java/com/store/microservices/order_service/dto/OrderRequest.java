package com.store.microservices.order_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Record for OrderRequest.
 * Represents a request to place an order, including items, shipping details, and user information.
 */
public record OrderRequest(
        List<OrderItem> items,
        BigDecimal total,
        String shippingAddress,
        LocalDate date,
        UserDetails userDetails
) {
    /**
     * Record for user details in an order.
     */
    public record UserDetails(String email, String firstName, String lastName) {
    }

    /**
     * Record for an item in the order.
     */
    public record OrderItem(String skuCode, Integer quantity) {
    }
}
