package com.store.microservices.product.repository;

import com.store.microservices.product.model.Product;
import org.springframework.boot.origin.Origin;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.lang.model.util.Elements;

public interface ProductRepository extends MongoRepository<Product, String> {
    Origin findBySkuCode(String skuCode);
}
