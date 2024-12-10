package com.store.microservices.inventory_service.service;

import com.store.microservices.inventory_service.dto.WarehouseRequest;
import com.store.microservices.inventory_service.dto.WarehouseResponse;
import com.store.microservices.inventory_service.model.Warehouse;
import com.store.microservices.inventory_service.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing warehouse operations.
 * Provides methods to create, update, delete, and retrieve warehouse details.
 */
@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * Retrieves a list of all warehouses.
     *
     * @return list of Warehouse entities
     */
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    /**
     * Retrieves details of a warehouse by its ID.
     *
     * @param id the ID of the warehouse
     * @return a WarehouseResponse with warehouse details
     * @throws RuntimeException if the warehouse is not found
     */
    public WarehouseResponse getWarehouseById(Long id) {
        Optional<Warehouse> warehouse = warehouseRepository.findById(id);
        if (warehouse.isEmpty()) {
            throw new RuntimeException("Warehouse not found");
        }
        return mapToResponse(warehouse.get());
    }

    /**
     * Creates a new warehouse.
     *
     * @param warehouseRequest the details of the warehouse to create
     * @return a WarehouseResponse with details of the created warehouse
     */
    public WarehouseResponse createWarehouse(WarehouseRequest warehouseRequest) {
        Warehouse warehouse = mapToEntity(warehouseRequest);
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return mapToResponse(savedWarehouse);
    }

    /**
     * Updates an existing warehouse with the provided details.
     *
     * @param id               the ID of the warehouse to update
     * @param updatedWarehouse the updated warehouse details
     * @return a WarehouseResponse with details of the updated warehouse
     * @throws RuntimeException if the warehouse is not found
     */
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest updatedWarehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(id);
        if (warehouseOptional.isEmpty()) {
            throw new RuntimeException("Warehouse not found");
        }

        Warehouse warehouse = warehouseOptional.get();

        // Update fields only if they are not null
        if (updatedWarehouse.getName() != null) {
            warehouse.setName(updatedWarehouse.getName());
        }
        if (updatedWarehouse.getAddress() != null) {
            warehouse.setAddress(updatedWarehouse.getAddress());
        }
        if (updatedWarehouse.getManagerName() != null) {
            warehouse.setManagerName(updatedWarehouse.getManagerName());
        }

        Warehouse updatedEntity = warehouseRepository.save(warehouse);
        return mapToResponse(updatedEntity);
    }

    /**
     * Deletes a warehouse by its ID.
     *
     * @param id the ID of the warehouse to delete
     * @return a success message if deletion is successful
     * @throws RuntimeException if the warehouse does not exist
     */
    public String deleteWarehouse(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new RuntimeException("Warehouse with ID " + id + " does not exist");
        }
        warehouseRepository.deleteById(id);
        return "Warehouse with ID " + id + " has been deleted successfully";
    }

    /**
     * Maps a WarehouseRequest DTO to a Warehouse entity.
     *
     * @param warehouseRequest the request object with warehouse details
     * @return a Warehouse entity
     */
    private Warehouse mapToEntity(WarehouseRequest warehouseRequest) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(warehouseRequest.getName());
        warehouse.setAddress(warehouseRequest.getAddress());
        warehouse.setManagerName(warehouseRequest.getManagerName());
        return warehouse;
    }

    /**
     * Maps a Warehouse entity to a WarehouseResponse DTO.
     *
     * @param warehouse the Warehouse entity
     * @return a WarehouseResponse with warehouse details
     */
    private WarehouseResponse mapToResponse(Warehouse warehouse) {
        WarehouseResponse warehouseResponse = new WarehouseResponse();
        warehouseResponse.setId(warehouse.getId());
        warehouseResponse.setName(warehouse.getName());
        warehouseResponse.setAddress(warehouse.getAddress());
        warehouseResponse.setManagerName(warehouse.getManagerName());
        return warehouseResponse;
    }
}
