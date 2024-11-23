package com.store.microservices.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(value = "category")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Category {

    @Id
    private String id;
    @Indexed(unique = true)
    private String skuCode;
    private String name;

}
