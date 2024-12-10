package com.store.microservices.inventory_service.controller;

import com.store.microservices.inventory_service.dto.InventoryResponse;
import com.store.microservices.inventory_service.dto.OrderRequest;
import com.store.microservices.inventory_service.model.Inventory;
import com.store.microservices.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing inventory-related operations, including adding and removing products,
 * checking stock availability, adjusting quantities, and handling warehouse changes.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Adds a product to the inventory or returns existing product details if it already exists.
     *
     * @param skuCode the SKU code of the product to add
     * @return true if the product was added or retrieved successfully
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
     * Deletes a product from the inventory by its SKU code.
     *
     * @param skuCode the SKU code of the product to delete
     * @return true if deletion was successful
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
     * Retrieves the entire inventory list.
     *
     * @return a list of all inventory items
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
     * Checks if a product with given SKU code is in stock with at least the specified quantity.
     *
     * @param skuCode the SKU code of the product
     * @param quantity the required quantity
     * @return an InventoryResponse indicating stock availability and status
     */
    @GetMapping("/stocks")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse isInStock(@RequestParam String skuCode, @RequestParam Integer quantity) {
        log.info("Checking stock for SKU code: {}, Quantity: {}", skuCode, quantity);
        InventoryResponse response = inventoryService.isInStock(skuCode, quantity);
        log.info("Stock check for SKU code: {} completed, Response: {}", skuCode, response);
        return response;
    }

    /**
     * Retrieves the current quantity available for a product.
     *
     * @param skuCode the SKU code of the product
     * @return the available quantity
     */
    @GetMapping("/product-quantities/{skuCode}")
    @ResponseStatus(HttpStatus.OK)
    public Integer getProductQuantity(@PathVariable String skuCode) {
        log.info("Fetching quantity for SKU code: {}", skuCode);
        Integer quantity = inventoryService.getProductQuantity(skuCode);
        log.info("Fetched quantity: {} for SKU code: {}", quantity, skuCode);
        return quantity;
    }

    /**
     * Deducts a specified quantity from the stock of a given product.
     *
     * @param skuCode the SKU code of the product
     * @param quantity the quantity to deduct
     * @return an InventoryResponse with updated stock details
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
     * Checks if an entire order can be fulfilled and updates the stock accordingly.
     *
     * @param orderRequests a list of OrderRequest objects representing the order
     * @return true if the entire order can be fulfilled, false otherwise
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
     * Retrieves a list of items that are currently low in stock.
     *
     * @return a list of low-stock items
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
     * Restocks the inventory for a given SKU code by setting its new quantity.
     *
     * @param skuCode the SKU code of the product
     * @param quantity the new quantity to set
     * @return an InventoryResponse with updated product details
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
     * Increments the stock for given SKUs after processing an order by distributing additional quantities.
     *
     * @param orderRequests a list of OrderRequest objects with SKUs and quantities to add
     * @return true if restocking is successful
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
     * Changes the warehouse location for a given product.
     *
     * @param skuCode the SKU code of the product
     * @param location the new location to set
     * @return an InventoryResponse with updated product details
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
