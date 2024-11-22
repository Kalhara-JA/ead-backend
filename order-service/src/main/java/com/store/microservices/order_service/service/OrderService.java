package com.store.microservices.order_service.service;


import com.store.microservices.order_service.client.InventoryClient;
import com.store.microservices.order_service.dto.OrderRequest;
import com.store.microservices.order_service.dto.OrderResponse;
import com.store.microservices.order_service.event.OrderPlacedEvent;
import com.store.microservices.order_service.model.Order;
import com.store.microservices.order_service.model.OrderItem;
import com.store.microservices.order_service.repository.OrderItemRepository;
import com.store.microservices.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    public void placeOrder(OrderRequest orderRequest) {
       // var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString()); // Generate unique order number
        order.setOrderDate(orderRequest.date()); // Set order date to current date
        order.setUserEmail(orderRequest.userDetails().email()); // Set user's email
        order.setStatus("PENDING");
        order.setShippingAddress(orderRequest.shippingAddress());// Default status
        order.setTotal(orderRequest.total()); // Calculate total order price

        // Map OrderRequest items to OrderItem entities and associate them with the order
        List<OrderItem> orderItems = orderRequest.items().stream()
                .map(item -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setSkuCode(item.skuCode());
                    orderItem.setQuantity(item.quantity());
                    orderItem.setOrder(order); // Associate the item with the order
                    return orderItem;
                }).toList();

        // Save Order and OrderItems
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(order.getOrderNumber(), orderRequest.userDetails().email());
        log.info("Staring to send OrderPlacedEvent {}",orderPlacedEvent);
        kafkaTemplate.send("order-placed", orderPlacedEvent);
        log.info("Ending to send OrderPlacedEvent {}", orderPlacedEvent);
    }

    public List<OrderResponse> getAllOrders() {
        // Fetch all orders from the database
        // Fetch all orders from the database
        List<Order> orders = orderRepository.findAll();
        // Map orders to OrderResponse
        return orders.stream()
                .map(order -> {
                    // Fetch associated OrderItems from the database
                    List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);

                    // Map to OrderResponse DTO
                    return new OrderResponse(
                            order.getId(),
                            order.getOrderNumber(),
                            orderItems.stream()
                                    .map(item -> new OrderResponse.OrderItem(item.getSkuCode(), item.getQuantity()))
                                    .toList(),
                            order.getTotal(),
                            order.getOrderDate(),
                            order.getShippingAddress(),
                            order.getStatus()
                    );
                })
                .toList();

    }

    public  OrderResponse getOrderById(Long id) {

        return orderRepository.findById(id)
                .map(order -> {
                    // Fetch associated OrderItems from the database
                    List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);

                    // Map to OrderResponse DTO
                    return new OrderResponse(
                            order.getId(),
                            order.getOrderNumber(),
                            orderItems.stream()
                                    .map(item -> new OrderResponse.OrderItem(item.getSkuCode(), item.getQuantity()))
                                    .toList(),
                            order.getTotal(),
                            order.getOrderDate(),
                            order.getShippingAddress(),
                            order.getStatus()
                    );
                })
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public  List<OrderResponse> getOrderByUser(String email) {
        // Fetch all orders from the database
        // Fetch all orders from the database
        List<Order> orders = orderRepository.findAllByUserEmail(email);
        // Map orders to OrderResponse
        return orders.stream()
                .map(order -> {
                    // Fetch associated OrderItems from the database
                    List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);

                    // Map to OrderResponse DTO
                    return new OrderResponse(
                            order.getId(),
                            order.getOrderNumber(),
                            orderItems.stream()
                                    .map(item -> new OrderResponse.OrderItem(item.getSkuCode(), item.getQuantity()))
                                    .toList(),
                            order.getTotal(),
                            order.getOrderDate(),
                            order.getShippingAddress(),
                            order.getStatus()
                    );
                })
                .toList();

    }

    public String updateOrderStatus(Long id, String status){
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Update the status
        order.setStatus(status);

        // Save the updated order back to the database
        orderRepository.save(order);

        // Return a success message
        return "Order status updated successfully to: " + status;
    }

}
