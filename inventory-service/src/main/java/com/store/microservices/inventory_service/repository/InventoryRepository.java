package com.store.microservices.inventory_service.repository;

import com.store.microservices.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Inventory entities.
 * Provides methods for custom queries and CRUD operations.
 */
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    boolean existsBySkuCodeAndQuantityIsGreaterThanEqual(String skuCode, int quantity);

    @Query("SELECT i FROM Inventory i WHERE i.skuCode = :skuCode")
    Optional<Inventory> findBySkuCode(@Param("skuCode") String skuCode);

    List<Inventory> findByQuantityLessThanEqual(Integer threshold);

    List<Inventory> findBySkuCodeOrderByQuantityDesc(String skuCode);

    void deleteBySkuCode(String skuCode);
}
