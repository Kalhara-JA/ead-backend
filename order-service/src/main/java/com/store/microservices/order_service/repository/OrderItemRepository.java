package com.store.microservices.order_service.repository;

import com.store.microservices.order_service.model.Order;
import com.store.microservices.order_service.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing OrderItem entities.
 * Provides methods to perform CRUD operations and custom queries.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Retrieves all order items associated with a specific order.
     *
     * @param order the order entity
     * @return a list of order items linked to the given order
     */
    List<OrderItem> findAllByOrder(Order order);

}
