package com.store.microservices.inventory_service.controller;

import com.store.microservices.inventory_service.dto.WarehouseRequest;
import com.store.microservices.inventory_service.dto.WarehouseResponse;
import com.store.microservices.inventory_service.model.Warehouse;
import com.store.microservices.inventory_service.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing warehouse operations.
 * Provides endpoints to perform CRUD operations on warehouse entities.
 */
@RestController
@RequestMapping("/api/v1/inventory/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * Retrieves a list of all warehouses.
     *
     * @return a list of warehouse entities
     */
    @GetMapping
    public List<Warehouse> getAllWarehouses() {
        return warehouseService.getAllWarehouses();
    }

    /**
     * Retrieves details of a specific warehouse by ID.
     *
     * @param id the ID of the warehouse
     * @return details of the warehouse as a WarehouseResponse
     */
    @GetMapping("/{id}")
    public WarehouseResponse getWarehouseById(@PathVariable Long id) {
        return warehouseService.getWarehouseById(id);
    }

    /**
     * Creates a new warehouse entity.
     *
     * @param warehouseRequest the details of the warehouse to create
     * @return the created warehouse as a WarehouseResponse
     */
    @PostMapping
    public WarehouseResponse createWarehouse(@RequestBody WarehouseRequest warehouseRequest) {
        return warehouseService.createWarehouse(warehouseRequest);
    }

    /**
     * Updates the details of an existing warehouse.
     *
     * @param id               the ID of the warehouse to update
     * @param warehouseRequest the updated warehouse details
     * @return the updated warehouse as a WarehouseResponse
     */
    @PutMapping("/{id}")
    public WarehouseResponse updateWarehouse(@PathVariable Long id, @RequestBody WarehouseRequest warehouseRequest) {
        return warehouseService.updateWarehouse(id, warehouseRequest);
    }

    /**
     * Deletes a warehouse by ID.
     *
     * @param id the ID of the warehouse to delete
     * @return a confirmation message as a string
     */
    @DeleteMapping("/{id}")
    public String deleteWarehouse(@PathVariable Long id) {
        return warehouseService.deleteWarehouse(id);
    }
}
