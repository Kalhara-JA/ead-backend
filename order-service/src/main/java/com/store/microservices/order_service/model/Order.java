package com.store.microservices.order_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "t_orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    private String orderNumber;
    private BigDecimal total;
    private String userEmail;
    private LocalDate orderDate;
    private String shippingAddress;

    private String paymentStatus;

    private String deliveryStatus;


}
