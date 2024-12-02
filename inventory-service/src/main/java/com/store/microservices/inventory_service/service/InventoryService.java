package com.store.microservices.inventory_service.service;


import com.store.microservices.inventory_service.dto.InventoryRequest;
import com.store.microservices.inventory_service.dto.InventoryResponse;
import com.store.microservices.inventory_service.dto.OrderRequest;
import com.store.microservices.inventory_service.model.Inventory;
import com.store.microservices.inventory_service.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private static final int LOW_STOCK_THRESHOLD = 5;


    public Boolean isInStock(String skuCode, int quantity) {
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
        Boolean inStock =item.getQuantity() >= quantity;
        String status = determineStatus(item.getQuantity());
        return InventoryResponse.builder()
                .skuCode(skuCode)
                .isInStock(inStock)
                .availableQuantity(item.getQuantity())
                .status(status)
                .build();
    }

    @Transactional(readOnly = true)
    public Integer getProductQuantity(String skuCode){
        Optional<Inventory> inventory = inventoryRepository.findBySkuCode(skuCode);
        return inventory.map(Inventory::getQuantity).orElse(0);
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
    public Boolean orderIsInStock(OrderRequest[] orderRequests) {
        for (OrderRequest orderRequest : orderRequests) {
            int remainingQuantity = orderRequest.getQuantity();

            // Get all inventory records for the given SKU, sorted by quantity descending
            List<Inventory> inventories = inventoryRepository.findBySkuCodeOrderByQuantityDesc(orderRequest.getSkuCode());
            if (inventories.isEmpty()) {
                return false; // No stock available for this SKU
            }

            for (Inventory inventory : inventories) {
                if (remainingQuantity <= 0) break;

                if (inventory.getQuantity() >= remainingQuantity) {
                    // Deduct the remaining quantity from this warehouse
                    inventory.setQuantity(inventory.getQuantity() - remainingQuantity);
                    inventory.setStatus(determineStatus(inventory.getQuantity()));
                    inventoryRepository.save(inventory);
                    remainingQuantity = 0;
                } else {
                    // Deduct all stock from this warehouse and move to the next one
                    remainingQuantity -= inventory.getQuantity();
                    inventory.setQuantity(0);
                    inventory.setStatus("Out of Stock");
                    inventoryRepository.save(inventory);
                }
            }

            if (remainingQuantity > 0) {
                return false; // Insufficient stock across all warehouses
            }
        }
        return true; // Order successfully fulfilled
    }

    public Boolean restockInventory(OrderRequest[] orderRequests) {
        for (OrderRequest orderRequest : orderRequests) {
            int remainingQuantity = orderRequest.getQuantity();
            int MAX_WAREHOUSE_CAPACITY = 500;

            // Get all inventory records for the given SKU, sorted by quantity descending
            List<Inventory> inventories = inventoryRepository.findBySkuCodeOrderByQuantityDesc(orderRequest.getSkuCode());

            // If no inventory exists, create a new inventory item
            if (inventories.isEmpty()) {
                Inventory newInventory = new Inventory();
                newInventory.setSkuCode(orderRequest.getSkuCode());
                newInventory.setQuantity(remainingQuantity);
                newInventory.setStatus(determineStatus(remainingQuantity));
                inventoryRepository.save(newInventory);
                continue;
            }

            // Distribute the restock quantity across existing inventories
            for (Inventory inventory : inventories) {
                if (remainingQuantity <= 0) break;

                // Add the remaining quantity to this inventory
                int spaceAvailable = MAX_WAREHOUSE_CAPACITY - inventory.getQuantity();
                int quantityToAdd = Math.min(remainingQuantity, spaceAvailable);

                inventory.setQuantity(inventory.getQuantity() + quantityToAdd);
                inventory.setStatus(determineStatus(inventory.getQuantity()));
                inventoryRepository.save(inventory);

                remainingQuantity -= quantityToAdd;
            }

            // If there's still remaining quantity, create a new inventory item
            if (remainingQuantity > 0) {
                Inventory newInventory = new Inventory();
                newInventory.setSkuCode(orderRequest.getSkuCode());
                newInventory.setQuantity(remainingQuantity);
                newInventory.setStatus(determineStatus(remainingQuantity));
                inventoryRepository.save(newInventory);
            }
        }

        return true;
    }



    public InventoryResponse addQuantity(String skuCode, Integer quantity) {
        // Check if the product exists
        try {
            Optional<Inventory> inventoryOptional = inventoryRepository.findBySkuCode(skuCode);

            if (skuCode == null || skuCode.trim().isEmpty()) {
                return InventoryResponse.builder()
                        .skuCode(skuCode)
                        .isInStock(false)
                        .availableQuantity(0)
                        .status("INVALID_SKU_CODE")
                        .build();
            }

            if (quantity == null || quantity <= 0) {
                return InventoryResponse.builder()
                        .skuCode(skuCode)
                        .isInStock(false)
                        .availableQuantity(0)
                        .status("INVALID_QUANTITY")
                        .build();
            }

        if (inventoryOptional.isEmpty()) {
            // Return a response indicating the product doesn't exist
            return InventoryResponse.builder()
                    .skuCode(skuCode)
                    .isInStock(false)
                    .availableQuantity(0)
                    .status("PRODUCT_NOT_FOUND") // Custom status to indicate the product is missing
                    .build();
        }

        Inventory inventory = inventoryOptional.get();

        // Update inventory quantity
        inventory.setQuantity(inventory.getQuantity() + quantity);

        // Update inventory status based on the new quantity
        inventory.setStatus(determineStatus(inventory.getQuantity()));

        // Save the updated inventory
        Inventory updatedInventory = inventoryRepository.save(inventory);

        // Build and return the response
        return InventoryResponse.builder()
                .skuCode(updatedInventory.getSkuCode())
                .isInStock(updatedInventory.getQuantity() > 0)
                .availableQuantity(updatedInventory.getQuantity())
                .status(updatedInventory.getStatus())
                .build();}
        catch (DataAccessException ex){
            log.error("Database error when restocking inventory", ex);
            return InventoryResponse.builder()
                    .skuCode(skuCode)
                    .isInStock(false)
                    .availableQuantity(0)
                    .status("DATABASE_ERROR")
                    .build();
        }
    }

}
