package com.store.microservices.product.repository;

import com.store.microservices.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository interface for managing Product entities.
 * Extends MongoRepository to provide CRUD operations on the "product" collection in MongoDB.
 */
public interface ProductRepository extends MongoRepository<Product, String> {

    /**
     * Deletes a product by its SKU code.
     *
     * @param skuCode the SKU code of the product to delete
     * @return the result of the delete operation
     */
    void deleteBySkuCode(String skuCode);

    /**
     * Finds a product by its SKU code.
     *
     * @param skuCode the SKU code of the product to find
     * @return the product entity, if found
     */
    Product findBySkuCode(String skuCode);
}
