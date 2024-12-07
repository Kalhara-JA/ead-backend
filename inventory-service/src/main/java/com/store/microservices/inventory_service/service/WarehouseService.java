package com.store.microservices.inventory_service.service;

import com.store.microservices.inventory_service.dto.WarehouseRequest;
import com.store.microservices.inventory_service.dto.WarehouseResponse;
import com.store.microservices.inventory_service.model.Warehouse;
import com.store.microservices.inventory_service.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public WarehouseResponse getWarehouseById(Long id) {
        Optional<Warehouse> warehouse =  warehouseRepository.findById(id);
        if(warehouse.isEmpty()){
            throw new RuntimeException("Warehouse not found");
        }
        return mapToResponse(warehouse.get());
    }

    public WarehouseResponse createWarehouse(WarehouseRequest warehouseRequest) {
        Warehouse warehouse = mapToEntity(warehouseRequest);
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return mapToResponse(savedWarehouse);

    }

    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest updatedWarehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(id);
        if (warehouseOptional.isEmpty()) {
            throw new RuntimeException("Warehouse not found");
        }

        Warehouse warehouse = warehouseOptional.get();

        // Only update non-null fields
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


    public String deleteWarehouse(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new RuntimeException("Warehouse with ID " + id + " does not exist");
        }
        warehouseRepository.deleteById(id);
        return "Warehouse with ID " + id + " has been deleted successfully";
    }
    

    private Warehouse mapToEntity(WarehouseRequest warehouseRequest) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(warehouseRequest.getName());
        warehouse.setAddress(warehouseRequest.getAddress());
        warehouse.setManagerName(warehouseRequest.getManagerName());
        return warehouse;
    }

    private WarehouseResponse mapToResponse(Warehouse warehouse) {
        WarehouseResponse warehouseResponse =  new WarehouseResponse(   );
        warehouseResponse.setId(warehouse.getId());
        warehouseResponse.setName(warehouse.getName());
        warehouseResponse.setAddress(warehouse.getAddress());
        warehouseResponse.setManagerName(warehouse.getManagerName());

        return warehouseResponse;
    }
}
