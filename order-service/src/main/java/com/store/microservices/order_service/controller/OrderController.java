package com.store.microservices.order_service.controller;


import com.store.microservices.order_service.dto.OrderRequest;
import com.store.microservices.order_service.dto.OrderResponse;
import com.store.microservices.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor

public class OrderController {
    private final OrderService orderService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest) {
        orderService.placeOrder(orderRequest);
        return "Order placed successfully";
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("getOrderById/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("getOrderByUser/{email}")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getOrderByUser(@PathVariable String email) {
        return orderService.getOrderByUser(email);
    }
    @PutMapping("doPayment/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String updateOrderStatus(@PathVariable Long id) {
        return orderService.doPayment(id);
    }
    @PutMapping("cancelPayment/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String cancelPayment(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }
    @PutMapping("changeDeliveryStatus/{id}/status/{status}")
    @ResponseStatus(HttpStatus.OK)
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return orderService.updateOrderDeliveryStatus(id, status);
    }

}
