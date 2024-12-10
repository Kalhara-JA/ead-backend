package com.store.microservices.order_service.service;

import com.store.microservices.order_service.client.InventoryClient;
import com.store.microservices.order_service.dto.*;
import com.store.microservices.order_service.event.OrderCancelEvent;
import com.store.microservices.order_service.event.OrderPlacedEvent;
import com.store.microservices.order_service.model.Order;
import com.store.microservices.order_service.model.OrderItem;
import com.store.microservices.order_service.repository.OrderItemRepository;
import com.store.microservices.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service class responsible for managing order operations.
 * Handles order placement, retrieval, updates, and events like cancellations and shipments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplatePlace;
    private final KafkaTemplate<String, OrderCancelEvent> kafkaTemplateCancel;

    /**
     * Places a new order.
     *
     * @param orderRequest the order request containing order details
     * @return the response with details of the placed order
     * @throws UserException if items are out of stock or another issue occurs
     */
    @Transactional
    public OrderPlaceResponse placeOrder(OrderRequest orderRequest) {
        try {
            List<InventoryRequest> inventoryItems = orderRequest.items().stream()
                    .map(item -> new InventoryRequest(item.skuCode(), item.quantity()))
                    .toList();

            InventoryResponse inventoryResponse = inventoryClient.decrementStock(inventoryItems);

            if (inventoryResponse.isInStock()) {
                Order order = new Order();
                order.setOrderNumber(UUID.randomUUID().toString());
                order.setOrderDate(orderRequest.date());
                order.setUserEmail(orderRequest.userDetails().email());
                order.setDeliveryStatus("PENDING");
                order.setShippingAddress(orderRequest.shippingAddress());
                order.setTotal(orderRequest.total());
                order.setPaymentStatus("UNPAID");

                List<OrderItem> orderItems = orderRequest.items().stream()
                        .map(item -> {
                            OrderItem orderItem = new OrderItem();
                            orderItem.setSkuCode(item.skuCode());
                            orderItem.setQuantity(item.quantity());
                            orderItem.setOrder(order);
                            return orderItem;
                        }).toList();

                orderRepository.save(order);
                orderItemRepository.saveAll(orderItems);

                OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(order.getOrderNumber(),
                        orderRequest.userDetails().email(),
                        orderRequest.userDetails().firstName(),
                        orderRequest.userDetails().lastName());

                kafkaTemplatePlace.send("order-placed", orderPlacedEvent);

                return new OrderPlaceResponse(order.getId(), order.getOrderNumber(), order.getTotal(), "");
            } else {
                throw new UserException("Out of Stock");
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Retrieves all orders along with their items.
     *
     * @return a list of all orders
     */
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderItem> orderItems = orderItemRepository.findAll();

        return orders.stream()
                .map(order -> new OrderResponse(
                        order.getId(),
                        order.getOrderNumber(),
                        orderItems.stream()
                                .filter(item -> item.getOrder().equals(order))
                                .map(item -> new OrderResponse.OrderItem(item.getSkuCode(), item.getQuantity()))
                                .toList(),
                        order.getTotal(),
                        order.getOrderDate(),
                        order.getShippingAddress(),
                        order.getPaymentStatus(),
                        order.getDeliveryStatus(),
                        order.getUserEmail()))
                .toList();
    }

    /**
     * Retrieves an order by its order number.
     *
     * @param orderNumber the unique order number
     * @return the details of the specified order
     */
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(order -> {
                    List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);
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
                            order.getUserEmail());
                })
                .orElseThrow(() -> new UserException("Order not found"));
    }

    /**
     * Processes payment for an order.
     *
     * @param id the ID of the order
     * @return a success message if payment is successful
     * @throws UserException if the order is not found or already paid
     */
    public String doPayment(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new UserException("Order not found"));

        if ("UNPAID".equals(order.getPaymentStatus())) {
            order.setPaymentStatus("PAID");
            orderRepository.save(order);
            return "Payment successfully done";
        } else {
            throw new UserException("Your already did payment");
        }
    }

    /**
     * Cancels an order.
     *
     * @param id the ID of the order
     * @return a success message if cancellation is successful
     * @throws UserException if the order cannot be canceled
     */
    public String cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new UserException("Order not found"));

        if ("PAID".equals(order.getPaymentStatus())) {
            throw new UserException("Can't cancel after payment");
        }

        List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);
        List<InventoryRequest> inventoryItems = orderItems.stream()
                .map(item -> new InventoryRequest(item.getSkuCode(), item.getQuantity()))
                .toList();

        if (inventoryClient.incrementStock(inventoryItems).isInStock()) {
            order.setPaymentStatus("CANCELED");
            order.setDeliveryStatus("CANCELED");
            orderRepository.save(order);

            OrderCancelEvent orderCancelEvent = new OrderCancelEvent(order.getOrderNumber(), order.getUserEmail());
            kafkaTemplateCancel.send("order-cancel", orderCancelEvent);

            return "Order canceled successfully";
        } else {
            throw new UserException("Order cancel failed");
        }
    }

    /**
     * Marks an order as shipped.
     *
     * @param id the ID of the order
     * @return a success message if the order is shipped
     * @throws UserException if the order cannot be shipped
     */
    public String shipOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new UserException("Order not found"));

        if (!"PAID".equals(order.getPaymentStatus())) {
            throw new UserException("Can't ship unpaid orders");
        }

        order.setDeliveryStatus("SHIPPED");
        orderRepository.save(order);

        return "Order shipped successfully";
    }

    /**
     * Marks an order as delivered.
     *
     * @param id the ID of the order
     * @return a success message if the order is delivered
     * @throws UserException if the order cannot be delivered
     */
    public String deliverOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new UserException("Order not found"));

        if (!"SHIPPED".equals(order.getDeliveryStatus())) {
            throw new UserException("Can't deliver before shipping");
        }

        order.setDeliveryStatus("DELIVERED");
        orderRepository.save(order);

        return "Order delivered successfully";
    }
}
