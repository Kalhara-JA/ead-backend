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
 * Service class responsible for business logic related to inventory management.
 * Handles operations such as adding, updating, deleting inventory, and stock checks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private static final int LOW_STOCK_THRESHOLD = 5;

    /**
     * Checks if a product is in stock with the specified quantity.
     *
     * @param skuCode  the SKU code of the product
     * @param quantity the required quantity
     * @return true if the product is in stock, false otherwise
     */
    public Boolean isInStock(String skuCode, int quantity) {
        return inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity);
    }

    /**
     * Adds a new product to the inventory or retrieves existing product details.
     *
     * @param skuCode the SKU code of the product
     * @return details of the added or existing product as an InventoryResponse
     */
    public InventoryResponse addProduct(String skuCode) {
        Optional<Inventory> existingItem = inventoryRepository.findBySkuCode(skuCode);
        if (existingItem.isPresent()) {
            Inventory item = existingItem.get();
            return buildInventoryResponse(item);
        }

        Inventory item = new Inventory();
        item.setSkuCode(skuCode);
        item.setQuantity(0);
        item.setLocation("Unknown");
        item.setStatus("OUT_OF_STOCK");
        Inventory savedItem = inventoryRepository.save(item);

        return buildInventoryResponse(savedItem);
    }

    /**
     * Deletes a product from the inventory.
     *
     * @param skuCode the SKU code of the product to delete
     * @return true if the product was deleted successfully, false otherwise
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
     * Retrieves a list of all inventory items.
     *
     * @return a list of all inventory items
     */
    @Transactional(readOnly = true)
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    /**
     * Retrieves the quantity of a specific product.
     *
     * @param skuCode the SKU code of the product
     * @return the available quantity of the product
     */
    @Transactional(readOnly = true)
    public Integer getProductQuantity(String skuCode) {
        return inventoryRepository.findBySkuCode(skuCode)
                .map(Inventory::getQuantity)
                .orElse(0);
    }

    /**
     * Adds inventory for a given product.
     *
     * @param request the inventory details
     * @return details of the updated inventory as an InventoryResponse
     */
    @Transactional
    public InventoryResponse addInventory(InventoryRequest request) {
        Optional<Inventory> existingItem = inventoryRepository.findBySkuCode(request.getSkuCode());
        Inventory item = existingItem.orElseGet(Inventory::new);

        item.setSkuCode(request.getSkuCode());
        item.setQuantity(item.getQuantity() + request.getQuantity());
        item.setLocation(request.getLocation());
        item.setStatus(determineStatus(item.getQuantity()));

        Inventory savedItem = inventoryRepository.save(item);
        return buildInventoryResponse(savedItem);
    }

    /**
     * Deducts stock for a given product.
     *
     * @param skuCode  the SKU code of the product
     * @param quantity the quantity to deduct
     * @return details of the updated inventory as an InventoryResponse
     * @throws RuntimeException if the product is not found or stock is insufficient
     */
    @Transactional
    public InventoryResponse reduceStock(String skuCode, Integer quantity) {
        Inventory item = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Product with SKU " + skuCode + " not found"));

        if (item.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock for product with SKU " + skuCode);
        }

        item.setQuantity(item.getQuantity() - quantity);
        item.setStatus(determineStatus(item.getQuantity()));
        Inventory updatedItem = inventoryRepository.save(item);

        return buildInventoryResponse(updatedItem);
    }

    /**
     * Retrieves a list of items with low stock levels.
     *
     * @return a list of inventory items with low stock
     */
    @Transactional(readOnly = true)
    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findByQuantityLessThanEqual(LOW_STOCK_THRESHOLD);
    }

    /**
     * Handles an order by verifying stock availability and deducting stock if available.
     *
     * @param orderRequests an array of order requests
     * @return true if the order can be fulfilled, false otherwise
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
                    inventory.setStatus("OUT_OF_STOCK");
                    inventoryRepository.save(inventory);
                }
            }

            if (remainingQuantity > 0) {
                return false;
            }
        }
        return true;
    }

    private String determineStatus(Integer quantity) {
        if (quantity == 0) {
            return "OUT_OF_STOCK";
        } else if (quantity < 10) {
            return "LOW_STOCK";
        } else {
            return "IN_STOCK";
        }
    }

    private InventoryResponse buildInventoryResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .skuCode(inventory.getSkuCode())
                .isInStock(inventory.getQuantity() > 0)
                .availableQuantity(inventory.getQuantity())
                .status(inventory.getStatus())
                .build();
    }
}
