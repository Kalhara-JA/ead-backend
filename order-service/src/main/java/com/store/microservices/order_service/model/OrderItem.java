package com.store.microservices.order_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an item in an order.
 * Maps order item data to the database and establishes a relationship with the Order entity.
 */
@Entity
@Table(name = "t_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skuCode;

    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false) // Foreign key to Order table
    private Order order;

}
