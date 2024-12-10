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
 * REST controller responsible for handling order-related operations such as placing orders,
 * retrieving orders, updating order status (payment, cancel, ship, deliver),
 * and fetching orders by user.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Places a new order based on the provided OrderRequest.
     *
     * @param orderRequest the order request containing product and user details
     * @return ResponseEntity containing OrderPlaceResponse with order details if successful, or an error response otherwise
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderPlaceResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        OrderPlaceResponse orderPlaceResponse;
        try {
            orderPlaceResponse = orderService.placeOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderPlaceResponse);
        } catch (UserException ex) {
            orderPlaceResponse = new OrderPlaceResponse(null, null, BigDecimal.ZERO, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(orderPlaceResponse);
        } catch (Exception ex) {
            orderPlaceResponse = new OrderPlaceResponse(null, null, BigDecimal.ZERO, "Order Place failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(orderPlaceResponse);
        }
    }

    /**
     * Retrieves all orders.
     *
     * @return a list of OrderResponse objects representing all orders
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    /**
     * Retrieves a specific order by its order number.
     *
     * @param orderNumber the unique order number
     * @return an OrderResponse object representing the requested order
     */
    @GetMapping("{orderNumber}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOrderById(@PathVariable String orderNumber) {
        return orderService.getOrderByOrderNumber(orderNumber);
    }

    /**
     * Retrieves all orders associated with a specific user's email.
     *
     * @param email the email of the user
     * @return a list of OrderResponse objects for that user
     */
    @GetMapping("user/{email}/orders")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getOrderByUser(@PathVariable String email) {
        return orderService.getOrderByUser(email);
    }

    /**
     * Updates the order status to "Paid" for a given order ID.
     *
     * @param id the ID of the order to update
     * @return ResponseEntity containing a success message if successful, or an error message otherwise
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
     * Cancels the specified order.
     *
     * @param id the ID of the order to cancel
     * @return ResponseEntity containing a success message if successful, or an error message otherwise
     */
    @PutMapping("{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> cancelOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(orderService.cancelOrder(id));
        } catch (UserException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cancel order failed..");
        }
    }

    /**
     * Marks the specified order as shipped.
     *
     * @param id the ID of the order to ship
     * @return ResponseEntity containing a success message if successful, or an error message otherwise
     */
    @PutMapping("{id}/ship")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> shipOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(orderService.shipOrder(id));
        } catch (UserException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to ship order");
        }
    }

    /**
     * Marks the specified order as delivered.
     *
     * @param id the ID of the order to deliver
     * @return ResponseEntity containing a success message if successful, or an error message otherwise
     */
    @PutMapping("{id}/deliver")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> deliverOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(orderService.deliverOrder(id));
        } catch (UserException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to deliver order");
        }
    }

}
