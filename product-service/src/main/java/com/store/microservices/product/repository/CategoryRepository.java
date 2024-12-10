package com.store.microservices.product.repository;

import com.store.microservices.product.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository interface for managing Category entities.
 * Extends MongoRepository to provide CRUD operations on the "category" collection in MongoDB.
 */
public interface CategoryRepository extends MongoRepository<Category, String> {
}
