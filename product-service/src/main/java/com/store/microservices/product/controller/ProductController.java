package com.store.microservices.product.controller;

import com.store.microservices.product.dto.*;
import com.store.microservices.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing products and categories in the Product Service.
 * Provides endpoints for creating, retrieving, updating, and deleting products and categories.
 */
@RestController
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Creates a new product.
     *
     * @param productRequest the product details
     * @return the created product details
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody ProductRequest productRequest) {
        return productService.createProduct(productRequest);
    }

    /**
     * Creates a new category.
     *
     * @param categoryRequest the category details
     * @return the created category details
     */
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@RequestBody CategoryRequest categoryRequest) {
        return productService.createCategory(categoryRequest);
    }

    /**
     * Retrieves all products with their inventory details.
     *
     * @return a list of products with inventory details
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param productId the ID of the product
     * @return the product details
     */
    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProductById(@PathVariable String productId) {
        return productService.getProductById(productId);
    }

    /**
     * Retrieves all categories.
     *
     * @return a list of all categories
     */
    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryResponse> getAllCategories() {
        return productService.getAllCategories();
    }

    /**
     * Updates a product by its ID.
     *
     * @param productId      the ID of the product
     * @param productRequest the updated product details
     * @return the updated product details
     */
    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse updateProduct(@PathVariable String productId, @RequestBody ProductRequest productRequest) {
        return productService.updateProduct(productId, productRequest);
    }

    /**
     * Updates the image of a product by its ID.
     *
     * @param productId the ID of the product
     * @param image     the new image URL or details
     * @return the updated product details
     */
    @PutMapping("/images/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse updateProductImage(@PathVariable String productId, @RequestBody String image) {
        return productService.updateProductImage(productId, image);
    }

    /**
     * Deletes a product by its ID.
     *
     * @param productId the ID of the product
     * @return a confirmation message
     */
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteProduct(@PathVariable String productId) {
        return productService.deleteProduct(productId);
    }
}
