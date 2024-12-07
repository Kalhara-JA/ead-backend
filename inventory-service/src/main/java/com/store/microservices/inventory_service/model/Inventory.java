package com.store.microservices.inventory_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="t_inventory",uniqueConstraints = @UniqueConstraint(columnNames = "skuCode"))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String skuCode;
    private Integer quantity;
    private String location;
    private String status;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    @JsonBackReference
    private Warehouse warehouse;


}