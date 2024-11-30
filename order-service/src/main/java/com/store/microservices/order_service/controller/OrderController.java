package com.store.microservices.order_service.controller;


import com.store.microservices.order_service.dto.OrderRequest;
import com.store.microservices.order_service.dto.OrderResponse;
import com.store.microservices.order_service.dto.UserException;
import com.store.microservices.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor

public class OrderController {
    private final OrderService orderService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest orderRequest) {
        try{
        orderService.placeOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body("Order placed successfully");
        }catch(UserException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Order place failed");
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

    @PatchMapping("payment/{id}")
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

    @PatchMapping("cancel/{id}")
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

    @PatchMapping("ship/{id}")
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


    @PatchMapping("deliver/{id}")
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
