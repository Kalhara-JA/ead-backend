package com.store.microservices.inventory_service.controller;

import com.store.microservices.inventory_service.dto.InventoryResponse;
import com.store.microservices.inventory_service.dto.OrderRequest;
import com.store.microservices.inventory_service.model.Inventory;
import com.store.microservices.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * REST controller for managing inventory operations.
 * Provides endpoints for CRUD operations, stock checks, and inventory adjustments.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Adds a new product to the inventory.
     *
     * @param skuCode the SKU code of the product
     * @return true if the product is successfully added
     */
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.OK)
    public Boolean addProduct(@RequestBody String skuCode) {
        log.info("Received request to add product with SKU code: {}", skuCode);
        InventoryResponse response = inventoryService.addProduct(skuCode);
        log.info("Product with SKU code: {} added successfully, Response: {}", skuCode, response);
        return response != null;
    }

    /**
     * Deletes a product from the inventory.
     *
     * @param skuCode the SKU code of the product
     * @return true if the product is successfully deleted
     */
    @DeleteMapping("/products")
    @ResponseStatus(HttpStatus.OK)
    public Boolean deleteProduct(@RequestBody String skuCode) {
        log.info("Received request to delete product with SKU code: {}", skuCode);
        Boolean response = inventoryService.deleteProduct(skuCode);
        log.info("Product with SKU code: {} deleted successfully, Response: {}", skuCode, response);
        return response != null;
    }

    /**
     * Fetches all inventory items.
     *
     * @return list of all inventory items
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> fetchAllInventory() {
        log.info("Fetching all inventory items");
        List<Inventory> inventoryList = inventoryService.getAllInventory();
        log.info("Fetched {} inventory items", inventoryList.size());
        return inventoryList;
    }

    /**
     * Checks stock availability for a product.
     *
     * @param skuCode  the SKU code of the product
     * @param quantity the required quantity
     * @return stock availability response
     */
    @GetMapping("/checkStock")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse isInStock(@RequestParam String skuCode, @RequestParam Integer quantity) {
        log.info("Checking stock for SKU code: {}, Quantity: {}", skuCode, quantity);
        InventoryResponse response = inventoryService.isInStock(skuCode, quantity);
        log.info("Stock check for SKU code: {} completed, Response: {}", skuCode, response);
        return response;
    }

    /**
     * Fetches the quantity of a specific product.
     *
     * @param skuCode the SKU code of the product
     * @return the quantity of the product
     */
    @GetMapping("/getProductQuantity/{skuCode}")
    @ResponseStatus(HttpStatus.OK)
    public Integer getProductQuantity(@PathVariable String skuCode) {
        log.info("Fetching quantity for SKU code: {}", skuCode);
        Integer quantity = inventoryService.getProductQuantity(skuCode);
        log.info("Fetched quantity: {} for SKU code: {}", quantity, skuCode);
        return quantity;
    }

    /**
     * Deducts stock for a product.
     *
     * @param skuCode  the SKU code of the product
     * @param quantity the quantity to deduct
     * @return stock deduction response
     */
    @PostMapping("/deduct")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse deductStock(
            @RequestParam String skuCode,
            @RequestParam Integer quantity
    ) {
        log.info("Received request to deduct stock for SKU code: {}, Quantity: {}", skuCode, quantity);
        try {
            InventoryResponse response = inventoryService.reduceStock(skuCode, quantity);
            log.info("Stock deduction for SKU code: {} completed, Response: {}", skuCode, response);
            return response;
        } catch (Exception ex) {
            log.error("Error during stock deduction for SKU code: {}", skuCode, ex);
            throw ex;
        }
    }

    /**
     * Checks stock and processes an order if items are in stock.
     *
     * @param orderRequests list of order requests
     * @return true if all items are in stock
     */
    @PostMapping("/check-stock")
    @ResponseStatus(HttpStatus.OK)
    public Boolean checkAndProcessOrder(@RequestBody List<OrderRequest> orderRequests) {
        log.info("Received stock check request for orders: {}", orderRequests);
        try {
            return inventoryService.orderIsInStock(orderRequests.toArray(new OrderRequest[0]));
        } catch (Exception e) {
            log.error("Error processing stock check request", e);
            return false;
        }
    }

    /**
     * Fetches items with low stock levels.
     *
     * @return list of low-stock items
     */
    @GetMapping("/low-stock")
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> getLowStockItems() {
        log.info("Fetching items with low stock levels");
        List<Inventory> lowStockItems = inventoryService.getLowStockItems();
        log.info("Fetched {} low-stock items", lowStockItems.size());
        return lowStockItems;
    }

    /**
     * Restocks inventory for a specific product.
     *
     * @param skuCode  the SKU code of the product
     * @param quantity the quantity to restock
     * @return restock response
     */
    @PostMapping("/restock")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse restockInventory(@RequestParam String skuCode, @RequestParam Integer quantity) {
        log.info("Received request to restock inventory for SKU code: {}, Quantity: {}", skuCode, quantity);
        InventoryResponse response = inventoryService.addQuantity(skuCode, quantity);
        log.info("Inventory restock for SKU code: {} completed, Response: {}", skuCode, response);
        return response;
    }

    /**
     * Adjusts inventory based on processed orders.
     *
     * @param orderRequests list of processed order requests
     * @return true if inventory is successfully adjusted
     */
    @PostMapping("/increment-stock")
    public Boolean restockProcessedOrder(@RequestBody List<OrderRequest> orderRequests) {
        log.info("Received stock increment request for orders: {}", orderRequests);
        try {
            return inventoryService.restockInventory(orderRequests.toArray(new OrderRequest[0]));
        } catch (Exception e) {
            log.error("Error processing stock increment request", e);
            return false;
        }
    }

    /**
     * Changes the warehouse location for a product.
     *
     * @param skuCode  the SKU code of the product
     * @param location the new warehouse location
     * @return warehouse change response
     */
    @PostMapping("/change-warehouse")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse changeWarehouse(@RequestParam String skuCode, @RequestParam String location) {
        log.info("Received request to change warehouse for SKU code: {}, Warehouse: {}", skuCode, location);
        InventoryResponse response = inventoryService.changeWarehouse(skuCode, location);
        log.info("Warehouse change for SKU code: {} completed, Response: {}", skuCode, response);
        return response;
    }
}
