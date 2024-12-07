package com.store.microservices.order_service.repository;


import com.store.microservices.order_service.model.Order;
import com.store.microservices.order_service.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrder(Order order);



}
