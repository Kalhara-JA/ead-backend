package com.store.microservices.inventory_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class WarehouseResponse {
    private Long id;
    private String name;
    private String address;
    private String managerName;
    private List<InventoryResponse> inventoryList;
}
