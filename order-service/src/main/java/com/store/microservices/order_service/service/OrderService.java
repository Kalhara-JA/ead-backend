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
 * Service class responsible for handling order operations, including placing orders,
 * retrieving orders, updating order statuses (payment, shipping, delivery), and cancelling orders.
 * This class interacts with the Inventory service to decrement and increment stock when orders
 * are placed or cancelled, and publishes order-related events to Kafka topics.
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
     * Places a new order based on the provided OrderRequest.
     * It checks the inventory for sufficient stock, creates a new order,
     * and publishes an OrderPlacedEvent if successful. If items are out of stock,
     * a UserException is thrown.
     *
     * @param orderRequest the OrderRequest containing details of the order to be placed
     * @return OrderPlaceResponse with details of the newly placed order
     */
    @Transactional
    public OrderPlaceResponse placeOrder(OrderRequest orderRequest) {
        try {
            // Map OrderRequest items to InventoryRequest DTOs
            List<InventoryRequest> inventoryItems = orderRequest.items().stream()
                    .map(item -> new InventoryRequest(item.skuCode(), item.quantity()))
                    .toList();

            // Decrement inventory stocks based on the requested order items
            InventoryResponse inventoryResponse = inventoryClient.decrementStock(inventoryItems);

            // Check if all items are in stock
            if (inventoryResponse.isInStock()) {
                // Create a new Order entity
                Order order = new Order();
                order.setOrderNumber(UUID.randomUUID().toString());
                order.setOrderDate(orderRequest.date());
                order.setUserEmail(orderRequest.userDetails().email());
                order.setDeliveryStatus("PENDING");
                order.setShippingAddress(orderRequest.shippingAddress());
                order.setTotal(orderRequest.total());
                order.setPaymentStatus("UNPAID");

                // Map order items and associate them with the newly created order
                List<OrderItem> orderItems = orderRequest.items().stream()
                        .map(item -> {
                            OrderItem orderItem = new OrderItem();
                            orderItem.setSkuCode(item.skuCode());
                            orderItem.setQuantity(item.quantity());
                            orderItem.setOrder(order);
                            return orderItem;
                        }).toList();

                // Save the Order and its OrderItems
                orderRepository.save(order);
                orderItemRepository.saveAll(orderItems);

                // Create the response object
                OrderPlaceResponse orderPlaceResponse = new OrderPlaceResponse(
                        order.getId(),
                        order.getOrderNumber(),
                        order.getTotal(),
                        ""
                );

                // Publish OrderPlacedEvent to Kafka topic
                try {
                    OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(
                            order.getOrderNumber(),
                            orderRequest.userDetails().email(),
                            orderRequest.userDetails().firstName(),
                            orderRequest.userDetails().lastName()
                    );
                    log.info("Starting to send OrderPlacedEvent {}", orderPlacedEvent);
                    kafkaTemplatePlace.send("order-placed", orderPlacedEvent);
                    log.info("Ending to send OrderPlacedEvent {}", orderPlacedEvent);
                } catch (Exception ex) {
                    log.error("Error sending OrderPlacedEvent to Kafka", ex);
                }

                return orderPlaceResponse;
            } else {
                // Throw exception if any item is out of stock
                throw new UserException("Out of Stock");
            }
        } catch (Exception ex) {
            // Rethrow any encountered exception
            throw ex;
        }
    }

    /**
     * Retrieves all orders along with their associated order items.
     * Converts entities to OrderResponse DTOs.
     *
     * @return a list of OrderResponse objects representing all orders
     */
    public List<OrderResponse> getAllOrders() {
        // Fetch all Orders and OrderItems
        List<Order> orders = orderRepository.findAll();
        List<OrderItem> orderItems = orderItemRepository.findAll();

        // Map Orders and their OrderItems to OrderResponse DTO
        return orders.stream()
                .map(order -> new OrderResponse(
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
                ))
                .toList();
    }

    /**
     * Retrieves a single order by its order number.
     * Throws a UserException if the order is not found.
     *
     * @param orderNumber the unique order number of the requested order
     * @return an OrderResponse DTO representing the requested order
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
                            order.getUserEmail()
                    );
                })
                .orElseThrow(() -> new UserException("Order not found"));
    }

    /**
     * Retrieves all orders associated with a given user's email.
     * Converts entities to OrderResponse DTOs.
     *
     * @param email the email of the user
     * @return a list of OrderResponse objects representing the user's orders
     */
    public List<OrderResponse> getOrderByUser(String email) {
        // Fetch all Orders and OrderItems for the specified user
        List<Order> orders = orderRepository.findAllByUserEmail(email);
        List<OrderItem> orderItems = orderItemRepository.findAll();

        // Map Orders and their OrderItems to OrderResponse DTO
        return orders.stream()
                .map(order -> new OrderResponse(
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
                ))
                .toList();
    }

    /**
     * Processes payment for an order identified by its ID.
     * The order must be in "UNPAID" status to be paid. Otherwise, a UserException is thrown.
     *
     * @param id the ID of the order
     * @return a success message if payment is completed successfully
     */
    public String doPayment(Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new UserException("Order not found"));

            if (order.getPaymentStatus().equals("UNPAID")) {
                order.setPaymentStatus("PAID");
                orderRepository.save(order);
                log.info("Payment Done for Order: {}", order.getOrderNumber());
                return "Payment successfully done";
            } else {
                // Throw an exception if the order is already paid
                throw new UserException("You already did payment");
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Cancels an order if it has not been paid. If the order is unpaid,
     * it increments the stock back in the inventory system, updates the order status to CANCELED,
     * and publishes an OrderCancelEvent. Throws a UserException if the order cannot be cancelled.
     *
     * @param id the ID of the order
     * @return a success message if the order is cancelled successfully
     */
    public String cancelOrder(Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new UserException("Order not found"));

            if (order.getPaymentStatus().equals("PAID")) {
                throw new UserException("Can't cancel after payment");
            }

            List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);
            List<InventoryRequest> inventoryItems = orderItems.stream()
                    .map(item -> new InventoryRequest(item.getSkuCode(), item.getQuantity()))
                    .toList();
            log.info("Inventory Items for cancellation: {}", inventoryItems);

            // Increment the Inventory
            if (inventoryClient.incrementStock(inventoryItems).isInStock()) {
                // Update Order status to CANCELED
                order.setPaymentStatus("CANCELED");
                order.setDeliveryStatus("CANCELED");
                orderRepository.save(order);

                // Publish OrderCancelEvent
                try {
                    OrderCancelEvent orderCancelEvent = new OrderCancelEvent(order.getOrderNumber(), order.getUserEmail());
                    log.info("Starting to send OrderCancelEvent {}", orderCancelEvent);
                    kafkaTemplateCancel.send("order-cancel", orderCancelEvent);
                    log.info("Finished sending OrderCancelEvent {}", orderCancelEvent);
                } catch (Exception ex) {
                    log.error("Error sending OrderCancelEvent to Kafka", ex);
                }

                return "Order canceled successfully";
            } else {
                throw new UserException("Order cancel failed");
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Marks an order as shipped if it has been paid for. If the order is not paid,
     * a UserException is thrown.
     *
     * @param id the ID of the order
     * @return a success message if the order is shipped successfully
     */
    public String shipOrder(Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new UserException("Order not found"));

            if (!order.getPaymentStatus().equals("PAID")) {
                throw new UserException("Can't ship unpaid orders");
            }

            order.setDeliveryStatus("SHIPPED");
            orderRepository.save(order);
            return "Order shipped successfully";
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Marks an order as delivered if it has been shipped. If the order is not in "SHIPPED" status,
     * a UserException is thrown.
     *
     * @param id the ID of the order
     * @return a success message if the order is delivered successfully
     */
    public String deliverOrder(Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new UserException("Order not found"));

            if (!order.getDeliveryStatus().equals("SHIPPED")) {
                throw new UserException("Can't deliver before shipping");
            }

            order.setDeliveryStatus("DELIVERED");
            orderRepository.save(order);
            return "Order delivered successfully";
        } catch (Exception ex) {
            throw ex;
        }
    }
}
