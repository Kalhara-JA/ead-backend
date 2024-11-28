package com.store.microservices.inventory_service.controller;

import com.store.microservices.inventory_service.dto.InventoryResponse;
import com.store.microservices.inventory_service.dto.OrderRequest;
import com.store.microservices.inventory_service.dto.StockCheckResponse;
import com.store.microservices.inventory_service.model.Inventory;
import com.store.microservices.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse addProduct(@RequestParam String skuCode) {
        log.info("Received request to add product with SKU code: {}", skuCode);
        InventoryResponse response = inventoryService.addProduct(skuCode);
        log.info("Product with SKU code: {} added successfully, Response: {}", skuCode, response);
        return response;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> fetchAllInventory() {
        log.info("Fetching all inventory items");
        List<Inventory> inventoryList = inventoryService.getAllInventory();
        log.info("Fetched {} inventory items", inventoryList.size());
        return inventoryList;
    }

    @GetMapping("/check-stock")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse isInStock(@RequestParam String skuCode, @RequestParam Integer quantity) {
        log.info("Checking stock for SKU code: {}, Quantity: {}", skuCode, quantity);
        InventoryResponse response = inventoryService.isInStock(skuCode, quantity);
        log.info("Stock check for SKU code: {} completed, Response: {}", skuCode, response);
        return response;
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> getAllInventory() {
        log.info("Fetching all inventory items");
        List<Inventory> inventoryList = inventoryService.getAllInventory();
        log.info("Fetched {} inventory items", inventoryList.size());
        return inventoryList;
    }

    @PostMapping("/deduct")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse deductInventory(@RequestParam String skuCode, @RequestParam Integer quantity) {
        log.info("Received request to deduct inventory for SKU code: {}, Quantity: {}", skuCode, quantity);
        InventoryResponse response = inventoryService.reduceStock(skuCode, quantity);
        log.info("Inventory deduction for SKU code: {} completed, Response: {}", skuCode, response);
        return response;
    }

    @PostMapping("/check-stock")
    public ResponseEntity<StockCheckResponse> checkAndProcessOrder(@RequestBody List<OrderRequest> orderRequests) {
        log.info("Received stock check request for orders: {}", orderRequests);
        try {
            boolean isInStock = inventoryService.orderIsInStock(orderRequests.toArray(new OrderRequest[0]));
            StockCheckResponse response = new StockCheckResponse(isInStock);

            if (isInStock) {
                log.info("Order is in stock and processed successfully");
                return ResponseEntity.ok(response);
            } else {
                log.warn("Order cannot be fulfilled due to insufficient stock");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
        } catch (Exception e) {
            log.error("Error processing stock check request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StockCheckResponse(false));
        }
    }

    @GetMapping("/low-stock")
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> getLowStockItems() {
        log.info("Fetching items with low stock levels");
        List<Inventory> lowStockItems = inventoryService.getLowStockItems();
        log.info("Fetched {} low-stock items", lowStockItems.size());
        return lowStockItems;
    }

    @PostMapping("/restock")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse restockInventory(@RequestParam String skuCode, @RequestParam Integer quantity) {
        log.info("Received request to restock inventory for SKU code: {}, Quantity: {}", skuCode, quantity);
        InventoryResponse response = inventoryService.addQuantity(skuCode, quantity);
        log.info("Inventory restock for SKU code: {} completed, Response: {}", skuCode, response);
        return response;
    }
    
}
