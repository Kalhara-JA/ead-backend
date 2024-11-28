package com.store.microservices.inventory_service.service;


import com.store.microservices.inventory_service.dto.InventoryRequest;
import com.store.microservices.inventory_service.dto.InventoryResponse;
import com.store.microservices.inventory_service.dto.OrderRequest;
import com.store.microservices.inventory_service.model.Inventory;
import com.store.microservices.inventory_service.repository.InventoryRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private static final int LOW_STOCK_THRESHOLD = 5;


    public boolean isInStock(String skuCode, int quantity) {
        return inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity);
    }

    public InventoryResponse addProduct(String skuCode){
        Inventory item = new Inventory();
        item.setSkuCode(skuCode);
        item.setQuantity(0);
        item.setLocation("Unknown");
        item.setStatus("OUT_OF_STOCK");
        Inventory savedItem = inventoryRepository.save(item);
        return InventoryResponse.builder()
                .skuCode(savedItem.getSkuCode())
                .isInStock(savedItem.getQuantity() > 0)
                .availableQuantity(savedItem.getQuantity())
                .status(savedItem.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public List<Inventory> getAllInventory(){
        return inventoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public InventoryResponse isInStock(String skuCode, Integer quantity){
        Optional<Inventory> inventory = inventoryRepository.findBySkuCode(skuCode);

        if(inventory.isEmpty()){
            return InventoryResponse.builder()
                    .skuCode(skuCode)
                    .isInStock(false)
                    .availableQuantity(0)
                    .status("Inventory not found")
                    .build();
        }

        Inventory item = inventory.get();
        boolean inStock =item.getQuantity() >= quantity;
        String status = determineStatus(item.getQuantity());
        return InventoryResponse.builder()
                .skuCode(skuCode)
                .isInStock(inStock)
                .availableQuantity(item.getQuantity())
                .status(status)
                .build();
    }

    @Transactional
    public InventoryResponse addInventory(InventoryRequest request){
        Optional<Inventory> existingItem = inventoryRepository.findBySkuCode(request.getSkuCode());
        Inventory item;
        if(existingItem.isPresent()){
            item=existingItem.get();
            item.setQuantity(item.getQuantity()+request.getQuantity());
        }else{
            item = new Inventory();
            item.setSkuCode(request.getSkuCode());
            item.setQuantity(request.getQuantity());
            item.setLocation(request.getLocation());
        }

        item.setStatus((determineStatus(item.getQuantity())));

        Inventory savedItem = inventoryRepository.save(item);

        return InventoryResponse.builder()
                .skuCode(savedItem.getSkuCode())
                .isInStock(savedItem.getQuantity() > 0)
                .availableQuantity(savedItem.getQuantity())
                .status(savedItem.getStatus())
                .build();

    }

    @Transactional
    public InventoryResponse reduceStock(String skuCode,Integer quantity){
        Optional<Inventory> inventory = inventoryRepository.findBySkuCode(skuCode);
        if(inventory.isEmpty()){
            throw new RuntimeException("Product with SKU "+ skuCode +" not found");
        }

        Inventory item = inventory.get();
        if(item.getQuantity()<quantity){
            throw new RuntimeException("Not enough stock for product with SKU "+skuCode);
        }
        item.setQuantity(item.getQuantity() -quantity);
        item.setStatus(determineStatus(item.getQuantity()));
        Inventory updatedItem = inventoryRepository.save(item);
        return InventoryResponse.builder()
                .skuCode(updatedItem.getSkuCode())
                .isInStock(updatedItem.getQuantity() > 0)
                .availableQuantity(updatedItem.getQuantity())
                .status(updatedItem.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findByQuantityLessThanEqual(LOW_STOCK_THRESHOLD);
    }

    private String determineStatus(Integer quantity) {
        if(quantity == 0){
            return "OUT_OF_STOCK";
        }else if(quantity < 10){
            return "LOW_STOCK";
        }else {
            return "IN_STOCK";
        }
    }

    @Transactional
    public boolean orderIsInStock(OrderRequest[] orderRequests) {
        for (OrderRequest orderRequest : orderRequests) {
            Optional<Inventory> inventory = inventoryRepository.findBySkuCode(orderRequest.getSkuCode());
            if(inventory.isEmpty()){
                return false;
            }
            Inventory item = inventory.get();
            if(item.getQuantity()<orderRequest.getQuantity()){
                return false;
            }
        }
        for(OrderRequest orderRequest : orderRequests){
            Optional<Inventory> inventory = inventoryRepository.findBySkuCode(orderRequest.getSkuCode());
            Inventory item = inventory.get();
            item.setQuantity(item.getQuantity() - orderRequest.getQuantity());
            item.setStatus(determineStatus(item.getQuantity()));
            inventoryRepository.save(item);
        }
        return true;
    }

    public InventoryResponse addQuantity(String skuCode, Integer quantity) {
        Optional<Inventory> inventory = inventoryRepository.findBySkuCode(skuCode);
        if(inventory.isEmpty()){
            throw new RuntimeException("Product with SKU "+ skuCode +" not found");
        }
        Inventory item = inventory.get();
        item.setQuantity(item.getQuantity() + quantity);
        item.setStatus(determineStatus(item.getQuantity()));
        Inventory updatedItem = inventoryRepository.save(item);
        return InventoryResponse.builder()
                .skuCode(updatedItem.getSkuCode())
                .isInStock(updatedItem.getQuantity() > 0)
                .availableQuantity(updatedItem.getQuantity())
                .status(updatedItem.getStatus())
                .build();
    }
}
