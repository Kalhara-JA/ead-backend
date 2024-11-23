package com.store.microservices.inventory_service.repository;

import com.store.microservices.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    boolean existsBySkuCodeAndQuantityIsGreaterThanEqual(String skuCode, int quantity);
    Optional<Inventory> findBySkuCode(String skuCode);
    List<Inventory> findByQuantityLessThanEqual(Integer threshold);

}