package com.store.microservices.order_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderRequest( List<OrderItem> items, BigDecimal total,String shippingAddress, LocalDate date, UserDetails userDetails) {

    public record UserDetails(String email, String firstName, String lastName) {
    }
    public record OrderItem(String skuCode, Integer quantity) {
    }
}
