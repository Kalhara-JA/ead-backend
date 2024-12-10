package com.store.microservices.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity representing a product category in the Product Service.
 * Stored in a MongoDB collection named "category".
 */
@Document(value = "category")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Category {

    @Id
    private String id;            // Unique identifier for the category
    @Indexed(unique = true)
    private String skuCode;       // Unique SKU code for the category
    private String name;          // Name of the category
}
