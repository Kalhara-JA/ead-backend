package com.store.microservices.order_service.dto;

public record InventoryRequest (
    String skuCode,
    Integer quantity,
    String location
){}
