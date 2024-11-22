package com.store.microservices.order_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderResponse(Long id, String orderNumber, List<OrderItem> items, BigDecimal total, LocalDate date,String shippingAddress, String status) {
    public record OrderItem(String skuCode, Integer quantity) {
    }

}
