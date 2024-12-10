package com.store.microservices.order_service.repository;

import com.store.microservices.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Order entities.
 * Provides CRUD operations and custom query methods for orders.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Retrieves all orders associated with a specific user email.
     *
     * @param email the email of the user
     * @return a list of orders placed by the user
     */
    List<Order> findAllByUserEmail(String email);

    /**
     * Retrieves an order by its order number.
     *
     * @param orderNumber the unique order number
     * @return an optional containing the order if found
     */
    Optional<Order> findByOrderNumber(String orderNumber);
}
