package com.store.microservices.inventory_service.controller;

import com.store.microservices.inventory_service.dto.InventoryRequest;
import com.store.microservices.inventory_service.dto.InventoryResponse;
import com.store.microservices.inventory_service.dto.OrderRequest;
import com.store.microservices.inventory_service.dto.StockCheckResponse;
import com.store.microservices.inventory_service.model.Inventory;
import com.store.microservices.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;  // This provides the log variable


import java.util.List;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> fetchAllInventory() {
        return inventoryService.getAllInventory();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse addInventory(@RequestBody InventoryRequest request){
        return inventoryService.addInventory(request);
    }

    @GetMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse isInStock(@RequestParam String skuCode, @RequestParam Integer quantity){
        return inventoryService.isInStock(skuCode,quantity);
    }


    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> getAllInventory(){
        return inventoryService.getAllInventory();
    }

    @PostMapping("/deduct")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse deductInventory(
            @RequestParam String skuCode,@RequestParam Integer quantity){

        return inventoryService.reduceStock(skuCode,quantity);
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
        return inventoryService.getLowStockItems();
    }

}
