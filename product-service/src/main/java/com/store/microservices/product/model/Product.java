package com.store.microservices.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * Entity representing a product in the Product Service.
 * Stored in a MongoDB collection named "product".
 */
@Document(value = "product")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Product {

    @Id
    private String id;            // Unique identifier for the product
    @Indexed(unique = true)
    private String skuCode;       // Unique SKU code for the product
    private String category;      // Category of the product
    private String brand;         // Brand of the product
    private String name;          // Name of the product
    private String image;         // Image URL or path for the product
    private String description;   // Description of the product
    private BigDecimal price;     // Price of the product
    private String updatedAt;     // Last updated timestamp
}
