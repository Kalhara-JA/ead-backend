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

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor

public class OrderController {
    private final OrderService orderService;




    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderPlaceResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        OrderPlaceResponse orderPlaceResponse;
        try {
            orderPlaceResponse = orderService.placeOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderPlaceResponse);
        } catch (UserException ex) {
            // Handle UserException, possibly with specific error details
            orderPlaceResponse = new OrderPlaceResponse(null, null, BigDecimal.ZERO, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(orderPlaceResponse);
        } catch (Exception ex) {
            // Handle general exceptions
            orderPlaceResponse = new OrderPlaceResponse(null, null, BigDecimal.ZERO, "Order Place failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(orderPlaceResponse);
        }
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("{orderNumber}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOrderById(@PathVariable String orderNumber) {
        return orderService.getOrderByOrderNumber(orderNumber);
    }

    @GetMapping("user/{email}/orders")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getOrderByUser(@PathVariable String email) {
        return orderService.getOrderByUser(email);
    }

    @PutMapping("{id}/payment")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> updateOrderStatus(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(orderService.doPayment(id));
        }catch (UserException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment Failed");
        }
    }

    @PutMapping("{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> cancelOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(orderService.cancelOrder(id));
        }catch (UserException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }catch (Exception ex){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cancel order failed..");
        }
    }

    @PutMapping("{id}/ship")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> shipOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body( orderService.shipOrder(id));
        } catch (UserException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to ship order");
        }

    }


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
