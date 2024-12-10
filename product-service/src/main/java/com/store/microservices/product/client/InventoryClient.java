package com.store.microservices.product.client;

import com.store.microservices.product.dto.InventoryResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * Client interface for interacting with the Inventory Service.
 * Provides methods for adding, retrieving, and deleting inventory products.
 */
public interface InventoryClient {

    /**
     * Adds a product to the inventory.
     *
     * @param skuCode the SKU code of the product to add
     * @return true if the product was successfully added, false otherwise
     */
    @PostExchange("/api/v1/inventory/products")
    Boolean addProductToInventory(@RequestBody String skuCode);

    /**
     * Retrieves the quantity of a product in the inventory by its SKU code.
     *
     * @param skuCode the SKU code of the product
     * @return the quantity of the product in the inventory
     */
    @GetExchange("/api/v1/inventory/getProductQuantity/{skuCode}")
    Integer getProductQuantity(@PathVariable String skuCode);

    /**
     * Retrieves all inventory items.
     *
     * @return a list of all inventory items as InventoryResponse
     */
    @GetExchange("/api/v1/inventory/all")
    List<InventoryResponse> getAllInventory();

    /**
     * Deletes a product from the inventory by its SKU code.
     *
     * @param skuCode the SKU code of the product to delete
     * @return true if the product was successfully deleted, false otherwise
     */
    @DeleteExchange("/api/v1/inventory/products")
    Boolean deleteProductFromInventory(@RequestBody String skuCode);

}
