package com.store.microservices.order_service.service;


import com.store.microservices.order_service.client.InventoryClient;
import com.store.microservices.order_service.dto.*;
import com.store.microservices.order_service.event.OrderPlacedEvent;
import com.store.microservices.order_service.model.Order;
import com.store.microservices.order_service.model.OrderItem;
import com.store.microservices.order_service.repository.OrderItemRepository;
import com.store.microservices.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void placeOrder(OrderRequest orderRequest) {
        try {
            //Map OrderRequest items to InventoryRequest DTOs
        List<InventoryRequest> inventoryItems = orderRequest.items().stream()
                .map(item -> {
                    return new InventoryRequest(item.skuCode(), item.quantity(), "DECREMENT");
                }).toList();

        //Decrement Inventory
        InventoryResponse inventoryResponse = inventoryClient.decrementStock(inventoryItems);


        // Check if all items are in stock
        if (inventoryResponse.isInStock()) {
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setOrderDate(orderRequest.date());
            order.setUserEmail(orderRequest.userDetails().email());
            order.setDeliveryStatus("PENDING");
            order.setShippingAddress(orderRequest.shippingAddress());
            order.setTotal(orderRequest.total());
            order.setPaymentStatus("UNPAID");

            //Map OrderRequest items to OrderItem entities and associate them with the order
            List<OrderItem> orderItems = orderRequest.items().stream()
                    .map(item -> {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setSkuCode(item.skuCode());
                        orderItem.setQuantity(item.quantity());
                        orderItem.setOrder(order);
                        return orderItem;
                    }).toList();

            // Save Order and OrderItems
            orderRepository.save(order);
            orderItemRepository.saveAll(orderItems);

            //Send OrderPlacedEvent
//            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(order.getOrderNumber(), orderRequest.userDetails().email(),orderRequest.userDetails().firstName(),orderRequest.userDetails().lastName());
//            log.info("Staring to send OrderPlacedEvent {}",orderPlacedEvent);
//            kafkaTemplate.send("order-placed", orderPlacedEvent);
//            log.info("Ending to send OrderPlacedEvent {}", orderPlacedEvent);
        }
        else {
            //Throw an exception if any item is out of stock
            throw new UserException("Out of Stock");
        }
        }catch (Exception ex){
            //Throw an exception if any error occurs
            throw ex;
        }

    }

    public List<OrderResponse> getAllOrders() {

        // Fetch all Orders and OrderItems from the database
        List<Order> orders = orderRepository.findAll();
        List<OrderItem> orderItems = orderItemRepository.findAll();

        return orders.stream()
                .map(order -> {
                    // Map to OrderResponse DTO
                    return new OrderResponse(
                            order.getId(),
                            order.getOrderNumber(),
                            orderItems.stream()
                                    .filter(item -> item.getOrder() == order)
                                    .map(item -> new OrderResponse.OrderItem(item.getSkuCode(), item.getQuantity()))
                                    .toList(),
                            order.getTotal(),
                            order.getOrderDate(),
                            order.getShippingAddress(),
                            order.getPaymentStatus(),
                            order.getDeliveryStatus(),
                            order.getUserEmail()
                    );
                })
                .toList();

    }

    public  OrderResponse getOrderByOrderNumber(String orderNumber) {

        return orderRepository.findByOrderNumber(orderNumber)
                .map(order -> {
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
                            order.getPaymentStatus(),
                            order.getDeliveryStatus(),
                            order.getUserEmail()
                    );
                })
                .orElseThrow(() -> new UserException("Order not found"));
    }

    public  List<OrderResponse> getOrderByUser(String email) {
        // Fetch all Orders and OrderItems from the database
        List<Order> orders = orderRepository.findAllByUserEmail(email);
        List<OrderItem> orderItems = orderItemRepository.findAll();

        // Map orders to OrderResponse
        return orders.stream()
                .map(order -> {
                    return new OrderResponse(
                            order.getId(),
                            order.getOrderNumber(),
                            orderItems.stream()
                                    .filter(item->item.getOrder()==order)
                                    .map(item -> new OrderResponse.OrderItem(item.getSkuCode(), item.getQuantity()))
                                    .toList(),
                            order.getTotal(),
                            order.getOrderDate(),
                            order.getShippingAddress(),
                            order.getPaymentStatus(),
                            order.getDeliveryStatus(),
                            order.getUserEmail()
                    );
                })
                .toList();

    }

    public String doPayment(Long id) {
        try {
            // Fetch the order from the database
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new UserException("Order not found"));

            // Check if the order is unpaid
            if (order.getPaymentStatus().equals("UNPAID")) {
            order.setPaymentStatus("PAID");
            orderRepository.save(order);

            log.info("Payment Done...{}",order.getOrderNumber());

            return "Payment successfully done";
        }
        else {
            //Throw an exception if the order is already paid
            throw new UserException("Your already did payment");
        }
    }catch (Exception ex) {
            //Throw an exception if any error occurs
        throw ex;
    }
    }

    public String cancelOrder(Long id){
        try {
            // Fetch the order from the database
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new UserException("Order not found"));

            if (order.getPaymentStatus().equals("PAID")) {
                throw new UserException( "Can't cancel after payment");
            }
            List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);
            List<InventoryRequest> inventoryItems = orderItems.stream()
                    .map(item -> {
                        return new InventoryRequest(item.getSkuCode(), item.getQuantity(), "DECREMENT");
                    }).toList();
            log.info("Inventory Items : {}",inventoryItems);

            //Increment Inventory
            if(inventoryClient.incrementStock(inventoryItems).isInStock()) {

                // Update the status
                order.setPaymentStatus("CANCELED");
                order.setDeliveryStatus("CANCELED");

                // Save the updated order back to the database
                orderRepository.save(order);

                // Return a success message
                return "Order Canceled successfully ";
            }else{
             throw new UserException("Order cancel failed");
            }
        }catch (Exception ex){
            //Throw an exception if any error occurs
            throw ex;
        }
    }

    public String shipOrder(Long id){
        try {
            // Fetch the order from the database
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new UserException("Order not found"));

            // Check if the order is unpaid or canceled
            if (!order.getPaymentStatus().equals("PAID")) {
                throw new UserException( "Can't ship unpaid orders");
            }

            // Update the status
            order.setDeliveryStatus("SHIPPED");

            // Save the updated order back to the database
            orderRepository.save(order);

            // Return a success message
            return "Order shipped successfully";
        }
        catch (Exception ex){
            //Throw an exception if any error occurs
            throw ex;
        }
    }

    public String deliverOrder(Long id){
        try {
            // Fetch the order from the database
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new UserException("Order not found"));

            if (!order.getDeliveryStatus().equals("SHIPPED")) {
                throw new UserException( "Can't deliver before shipping");
            }
            // Update the status
            order.setDeliveryStatus("DELIVERED");

            // Save the updated order back to the database
            orderRepository.save(order);

            // Return a success message
            return "Order delivered successfully";
        }
        catch (Exception ex){
            //Throw an exception if any error occurs
            throw ex;
        }
    }



}
