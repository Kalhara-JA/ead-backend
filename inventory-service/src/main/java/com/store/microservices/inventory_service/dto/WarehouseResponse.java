package com.store.microservices.inventory_service.dto;

import lombok.Data;
import java.util.List;

/**
 * Data Transfer Object (DTO) for warehouse-related responses.
 * Encapsulates the details of a warehouse, including its inventory.
 */
@Data
public class WarehouseResponse {
    private Long id; // Unique identifier for the warehouse
    private String name; // Name of the warehouse
    private String address; // Address of the warehouse
    private String managerName; // Name of the warehouse manager
    private List<InventoryResponse> inventoryList; // List of inventory items associated with the warehouse
}
