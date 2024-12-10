package com.store.microservices.inventory_service.repository;

import com.store.microservices.inventory_service.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing Warehouse entities.
 * Provides built-in CRUD operations through JpaRepository.
 */
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}
