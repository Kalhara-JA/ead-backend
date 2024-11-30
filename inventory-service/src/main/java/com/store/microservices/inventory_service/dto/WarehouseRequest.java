package com.store.microservices.inventory_service.dto;

import lombok.Data;

@Data
public class WarehouseRequest {
    private String name;
    private String address;
    private String managerName;
}
