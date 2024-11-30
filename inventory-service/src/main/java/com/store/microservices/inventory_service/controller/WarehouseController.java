package com.store.microservices.inventory_service.controller;

import com.store.microservices.inventory_service.dto.WarehouseRequest;
import com.store.microservices.inventory_service.dto.WarehouseResponse;
import com.store.microservices.inventory_service.model.Warehouse;
import com.store.microservices.inventory_service.service.WarehouseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/warehouses")
public class WarehouseController {
    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public List<Warehouse> getAllWarehouses() {
        return warehouseService.getAllWarehouses();
    }

    @GetMapping("/{id}")
    public WarehouseResponse getWarehouseById(@PathVariable Long id) {
        return warehouseService.getWarehouseById(id);
    }

    @PostMapping
    public WarehouseResponse createWarehouse(@RequestBody WarehouseRequest warehouseRequest) {
        return warehouseService.createWarehouse(warehouseRequest);
    }

    @PutMapping("/{id}")
    public WarehouseResponse updateWarehouse(@PathVariable Long id, @RequestBody WarehouseRequest warehouseRequest) {
        return warehouseService.updateWarehouse(id, warehouseRequest);
    }

    @DeleteMapping("/{id}")
    public String deleteWarehouse(@PathVariable Long id) {
        return warehouseService.deleteWarehouse(id);
    }
}
