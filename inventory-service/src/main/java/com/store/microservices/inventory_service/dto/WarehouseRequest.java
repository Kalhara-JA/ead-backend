package com.store.microservices.inventory_service.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for warehouse-related requests.
 * Encapsulates the details required to create or update a warehouse.
 */
@Data
public class WarehouseRequest {
    private String name; // Name of the warehouse
    private String address; // Address of the warehouse
    private String managerName; // Name of the warehouse manager
}
