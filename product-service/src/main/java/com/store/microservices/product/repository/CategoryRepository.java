package com.store.microservices.product.repository;

import com.store.microservices.product.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
}
