package com.store.microservices.order_service.controller;

import com.store.microservices.order_service.dto.OrderPlaceResponse;
import com.store.microservices.order_service.dto.OrderRequest;
import com.store.microservices.order_service.dto.OrderResponse;
import com.store.microservices.order_service.dto.UserException;
import com.store.microservices.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for handling order-related operations.
 * Provides endpoints to place, retrieve, and manage orders.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Places a new order.
     *
     * @param orderRequest the details of the order
     * @return the response with order placement details or an error message
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderPlaceResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        try {
            OrderPlaceResponse orderPlaceResponse = orderService.placeOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderPlaceResponse);
        } catch (UserException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OrderPlaceResponse(null, null, BigDecimal.ZERO, ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OrderPlaceResponse(null, null, BigDecimal.ZERO, "Order Place failed"));
        }
    }

    /**
     * Retrieves all orders.
     *
     * @return a list of all orders
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    /**
     * Retrieves an order by its order number.
     *
     * @param orderNumber the order number
     * @return the details of the order
     */
    @GetMapping("{orderNumber}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOrderById(@PathVariable String orderNumber) {
        return orderService.getOrderByOrderNumber(orderNumber);
    }

    /**
     * Retrieves orders for a specific user by their email.
     *
     * @param email the email of the user
     * @return a list of orders placed by the user
     */
    @GetMapping("user/{email}/orders")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getOrderByUser(@PathVariable String email) {
        return orderService.getOrderByUser(email);
    }

    /**
     * Updates the status of an order to "Paid".
     *
     * @param id the ID of the order
     * @return a response indicating success or failure
     */
    @PutMapping("{id}/payment")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> updateOrderStatus(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(orderService.doPayment(id));
        } catch (UserException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment Failed");
        }
    }

    /**
     * Cancels an order.
     *
     * @param id the ID of the order
     * @return a response indicating success or failure
     */
    @PutMapping("{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> cancelOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(orderService.cancelOrder(id));
        } catch (UserException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cancel order failed..");
        }
    }

    /**
     * Updates the status of an order to "Shipped".
     *
     * @param id the ID of the order
     * @return a response indicating success or failure
     */
    @PutMapping("{id}/ship")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> shipOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(orderService.shipOrder(id));
        } catch (UserException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to ship order");
        }
    }

    /**
     * Updates the status of an order to "Delivered".
     *
     * @param id the ID of the order
     * @return a response indicating success or failure
     */
    @PutMapping("{id}/deliver")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> deliverOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(orderService.deliverOrder(id));
        } catch (UserException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to deliver order");
        }
    }
}
