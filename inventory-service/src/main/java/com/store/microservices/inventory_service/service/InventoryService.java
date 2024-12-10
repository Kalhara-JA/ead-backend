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

import java.util.List;
import java.util.Optional;

/**
 * Service for managing inventory operations such as adding, deleting,
 * checking stock, updating quantities, fulfilling orders, and restocking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private static final int LOW_STOCK_THRESHOLD = 5;

    /**
     * Checks if a product with given SKU code has at least the specified quantity in stock.
     *
     * @param skuCode the SKU code of the product
     * @param quantity the required quantity
     * @return true if in stock, false otherwise
     */
    public Boolean isInStock(String skuCode, int quantity) {
        return inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity);
    }

    /**
     * Adds a new product if it does not exist, or returns details of the existing product.
     *
     * @param skuCode the SKU code of the product
     * @return the current inventory response for the product
     */
    public InventoryResponse addProduct(String skuCode) {
        Optional<Inventory> existingItem = inventoryRepository.findBySkuCode(skuCode);
        if (existingItem.isPresent()) {
            Inventory item = existingItem.get();
            return InventoryResponse.builder()
                    .skuCode(item.getSkuCode())
                    .isInStock(item.getQuantity() > 0)
                    .availableQuantity(item.getQuantity())
                    .status(item.getStatus())
                    .build();
        }

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

    /**
     * Deletes a product from the inventory by SKU code.
     *
     * @param skuCode the SKU code of the product
     * @return true if deletion was successful, false otherwise
     */
    @Transactional
    public Boolean deleteProduct(String skuCode) {
        Optional<Inventory> existingItem = inventoryRepository.findBySkuCode(skuCode);
        if (existingItem.isEmpty()) {
            log.warn("Attempted to delete non-existent product with SKU: {}", skuCode);
            return false;
        }
        try {
            inventoryRepository.deleteBySkuCode(skuCode);
            log.info("Product with SKU {} successfully deleted", skuCode);
            return true;
        } catch (Exception e) {
            log.error("Error deleting product with SKU: {}", skuCode, e);
            return false;
        }
    }

    /**
     * Retrieves the entire inventory list.
     *
     * @return list of all inventory items
     */
    @Transactional(readOnly = true)
    public List<Inventory> getAllInventory(){
        return inventoryRepository.findAll();
    }

    /**
     * Checks if a product is in stock with a specified quantity and returns a detailed response.
     *
     * @param skuCode the SKU code of the product
     * @param quantity the required quantity
     * @return InventoryResponse indicating stock availability and status
     */
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

    /**
     * Retrieves the current quantity for a product.
     *
     * @param skuCode the SKU code of the product
     * @return the current quantity, 0 if not found
     */
    @Transactional(readOnly = true)
    public Integer getProductQuantity(String skuCode){
        Optional<Inventory> inventory = inventoryRepository.findBySkuCode(skuCode);
        return inventory.map(Inventory::getQuantity).orElse(0);
    }

    /**
     * Adds inventory for a product. If the product doesn't exist, creates it.
     *
     * @param request the InventoryRequest with SKU code and quantity
     * @return InventoryResponse with updated product details
     */
    @Transactional
    public InventoryResponse addInventory(InventoryRequest request){
        Optional<Inventory> existingItem = inventoryRepository.findBySkuCode(request.getSkuCode());
        Inventory item;
        if(existingItem.isPresent()){
            item = existingItem.get();
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

    /**
     * Reduces stock for a product by a given quantity.
     *
     * @param skuCode the SKU code of the product
     * @param quantity the quantity to reduce
     * @return InventoryResponse with updated product details
     */
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

    /**
     * Retrieves a list of items that are low in stock.
     *
     * @return list of low stock items
     */
    @Transactional(readOnly = true)
    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findByQuantityLessThanEqual(LOW_STOCK_THRESHOLD);
    }

    /**
     * Determines the status of a product based on its quantity.
     *
     * @param quantity the product quantity
     * @return the status of the product: "OUT_OF_STOCK", "LOW_STOCK", or "IN_STOCK"
     */
    private String determineStatus(Integer quantity) {
        if(quantity == 0){
            return "OUT_OF_STOCK";
        }else if(quantity < 10){
            return "LOW_STOCK";
        }else {
            return "IN_STOCK";
        }
    }

    /**
     * Checks if an entire order can be fulfilled and reduces inventory accordingly.
     *
     * @param orderRequests array of OrderRequest representing products and their required quantities
     * @return true if the entire order can be fulfilled, false otherwise
     */
    @Transactional
    public Boolean orderIsInStock(OrderRequest[] orderRequests) {
        for (OrderRequest orderRequest : orderRequests) {
            int remainingQuantity = orderRequest.getQuantity();
            List<Inventory> inventories = inventoryRepository.findBySkuCodeOrderByQuantityDesc(orderRequest.getSkuCode());
            if (inventories.isEmpty()) {
                return false;
            }

            for (Inventory inventory : inventories) {
                if (remainingQuantity <= 0) break;

                if (inventory.getQuantity() >= remainingQuantity) {
                    inventory.setQuantity(inventory.getQuantity() - remainingQuantity);
                    inventory.setStatus(determineStatus(inventory.getQuantity()));
                    inventoryRepository.save(inventory);
                    remainingQuantity = 0;
                } else {
                    remainingQuantity -= inventory.getQuantity();
                    inventory.setQuantity(0);
                    inventory.setStatus("Out of Stock");
                    inventoryRepository.save(inventory);
                }
            }

            if (remainingQuantity > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Changes the warehouse location for a product.
     *
     * @param skuCode the SKU code of the product
     * @param location the new location
     * @return InventoryResponse with updated product details
     */
    @Transactional
    public InventoryResponse changeWarehouse(String skuCode, String location) {
        Optional<Inventory> inventory = inventoryRepository.findBySkuCode(skuCode);
        if (inventory.isEmpty()) {
            throw new RuntimeException("Product with SKU " + skuCode + " not found");
        }

        Inventory item = inventory.get();
        if (!item.getLocation().equals(location)) {
            item.setLocation(location);
            Inventory updatedItem = inventoryRepository.save(item);
            return InventoryResponse.builder()
                    .skuCode(updatedItem.getSkuCode())
                    .isInStock(updatedItem.getQuantity() > 0)
                    .availableQuantity(updatedItem.getQuantity())
                    .status(updatedItem.getStatus())
                    .build();
        }

        return InventoryResponse.builder()
                .skuCode(item.getSkuCode())
                .isInStock(item.getQuantity() > 0)
                .availableQuantity(item.getQuantity())
                .status(item.getStatus())
                .build();
    }

    /**
     * Restocks inventory for given SKUs and quantities, creating new inventory items if needed.
     *
     * @param orderRequests array of OrderRequest with SKUs and quantities to restock
     * @return true after restocking is performed
     */
    public Boolean restockInventory(OrderRequest[] orderRequests) {
        for (OrderRequest orderRequest : orderRequests) {
            int remainingQuantity = orderRequest.getQuantity();
            int MAX_WAREHOUSE_CAPACITY = 500;
            List<Inventory> inventories = inventoryRepository.findBySkuCodeOrderByQuantityDesc(orderRequest.getSkuCode());

            if (inventories.isEmpty()) {
                Inventory newInventory = new Inventory();
                newInventory.setSkuCode(orderRequest.getSkuCode());
                newInventory.setQuantity(remainingQuantity);
                newInventory.setStatus(determineStatus(remainingQuantity));
                inventoryRepository.save(newInventory);
                continue;
            }

            for (Inventory inventory : inventories) {
                if (remainingQuantity <= 0) break;
                int spaceAvailable = MAX_WAREHOUSE_CAPACITY - inventory.getQuantity();
                int quantityToAdd = Math.min(remainingQuantity, spaceAvailable);
                inventory.setQuantity(inventory.getQuantity() + quantityToAdd);
                inventory.setStatus(determineStatus(inventory.getQuantity()));
                inventoryRepository.save(inventory);
                remainingQuantity -= quantityToAdd;
            }

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

    /**
     * Updates the quantity of a given product.
     *
     * @param skuCode the SKU code of the product
     * @param quantity the new quantity
     * @return InventoryResponse with updated details or an error response if the update fails
     */
    public InventoryResponse addQuantity(String skuCode, Integer quantity) {
        if (skuCode == null || skuCode.trim().isEmpty()) {
            return createErrorResponse(skuCode, "INVALID_SKU_CODE");
        }
        if (quantity == null || quantity <= 0) {
            return createErrorResponse(skuCode, "INVALID_QUANTITY");
        }

        try {
            Optional<Inventory> inventoryOptional = inventoryRepository.findBySkuCode(skuCode);
            log.info("Inventory Optional: {}", inventoryOptional);
            if (inventoryOptional.isEmpty()) {
                log.warn("Product with SKU code {} not found in inventory.", skuCode);
                return createErrorResponse(skuCode, "PRODUCT_NOT_AVAILABLE");
            }

            Inventory inventory = inventoryOptional.get();
            log.info("Initial Inventory: {}", inventory);
            inventory.setQuantity(quantity);
            log.info("Updated Inventory: {}", inventory);
            inventory.setStatus(determineStatus(inventory.getQuantity()));
            Inventory updatedInventory = inventoryRepository.save(inventory);

            return InventoryResponse.builder()
                    .skuCode(updatedInventory.getSkuCode())
                    .isInStock(updatedInventory.getQuantity() > 0)
                    .availableQuantity(updatedInventory.getQuantity())
                    .status(updatedInventory.getStatus())
                    .build();

        } catch (DataAccessException ex) {
            log.error("Database error when restocking inventory for SKU: {}", skuCode, ex);
            return createErrorResponse(skuCode, "DATABASE_ERROR");
        } catch (Exception ex) {
            log.error("Unexpected error when restocking inventory for SKU: {}", skuCode, ex);
            return createErrorResponse(skuCode, "UNEXPECTED_ERROR");
        }
    }

    /**
     * Creates an error response for a given SKU code and error scenario.
     *
     * @param skuCode the SKU code of the product
     * @param errorCode the error code
     * @return InventoryResponse indicating an error
     */
    private InventoryResponse createErrorResponse(String skuCode, String errorCode) {
        return InventoryResponse.builder()
                .skuCode(skuCode)
                .isInStock(false)
                .availableQuantity(0)
                .status("ERROR")
                .build();
    }

}
