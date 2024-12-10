package com.store.microservices.product.service;

import com.store.microservices.product.client.InventoryClient;
import com.store.microservices.product.dto.*;
import com.store.microservices.product.model.Category;
import com.store.microservices.product.model.Product;
import com.store.microservices.product.repository.CategoryRepository;
import com.store.microservices.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing products and categories in the Product Service.
 * Handles operations like creating, updating, retrieving, and deleting products and categories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;
    private final CategoryRepository categoryRepository;

    /**
     * Creates a new product.
     *
     * @param productRequest the product details
     * @return the created product details
     * @throws RuntimeException if the product already exists or adding it to inventory fails
     */
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.info("Creating Product: {}", productRequest.skuCode());

        if (productRepository.findBySkuCode(productRequest.skuCode()) != null) {
            throw new RuntimeException("Product Already Exists!");
        }

        if (!inventoryClient.addProductToInventory(productRequest.skuCode())) {
            throw new RuntimeException("Product adding failed!");
        }

        Product product = Product.builder()
                .name(productRequest.name())
                .skuCode(productRequest.skuCode())
                .category(productRequest.category())
                .brand(productRequest.brand())
                .description(productRequest.description())
                .image(productRequest.image())
                .price(productRequest.price())
                .updatedAt(productRequest.updatedAt())
                .build();
        productRepository.save(product);

        log.info("Product Created Successfully!");
        return mapToProductResponse(product);
    }

    /**
     * Creates a new category.
     *
     * @param categoryRequest the category details
     * @return the created category details
     */
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        Category category = Category.builder()
                .name(categoryRequest.name())
                .skuCode(categoryRequest.skuCode())
                .build();
        categoryRepository.save(category);

        log.info("Category Created Successfully!");
        return mapToCategoryResponse(category);
    }

    /**
     * Retrieves all products with inventory data.
     *
     * @return a list of products with inventory details
     */
    public List<InventoryResponse> getAllProducts() {
        log.info("Fetching All Products");

        Map<String, InventoryResponse> inventoryMap = inventoryClient.getAllInventory()
                .stream()
                .collect(Collectors.toMap(InventoryResponse::skuCode, inventory -> inventory));

        return productRepository.findAll()
                .stream()
                .map(product -> mapToInventoryResponse(product, inventoryMap.getOrDefault(product.getSkuCode(), null)))
                .toList();
    }

    /**
     * Retrieves all categories.
     *
     * @return a list of all categories
     */
    public List<CategoryResponse> getAllCategories() {
        log.info("Fetching All Categories");
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param productId the product ID
     * @return the product details
     */
    public ProductResponse getProductById(String productId) {
        log.info("Fetching Product By ID");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));
        return mapToProductResponse(product);
    }

    /**
     * Retrieves the quantity of a product by SKU code.
     *
     * @param skuCode the SKU code
     * @return inventory details of the product
     */
    public InventoryResponse getProductQuantity(String skuCode) {
        log.info("Fetching Product Quantity");
        Integer quantity = inventoryClient.getProductQuantity(skuCode);
        Product product = productRepository.findBySkuCode(skuCode);

        if (product == null) {
            throw new RuntimeException("Product Not Found");
        }

        return mapToInventoryResponse(product, new InventoryResponse(
                product.getId(),
                product.getName(),
                product.getSkuCode(),
                product.getCategory(),
                product.getBrand(),
                product.getDescription(),
                product.getImage(),
                product.getPrice(),
                product.getUpdatedAt(),
                quantity));
    }

    /**
     * Updates an existing product by ID.
     *
     * @param productId      the product ID
     * @param productRequest the updated product details
     * @return the updated product details
     */
    public ProductResponse updateProduct(String productId, ProductRequest productRequest) {
        log.info("Updating Product");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));

        product.setName(productRequest.name());
        product.setSkuCode(productRequest.skuCode());
        product.setCategory(productRequest.category());
        product.setBrand(productRequest.brand());
        product.setDescription(productRequest.description());
        product.setImage(productRequest.image());
        product.setPrice(productRequest.price());
        product.setUpdatedAt(productRequest.updatedAt());

        productRepository.save(product);
        return mapToProductResponse(product);
    }

    /**
     * Updates the image of a product by ID.
     *
     * @param productId the product ID
     * @param image     the new image URL
     * @return the updated product details
     */
    public ProductResponse updateProductImage(String productId, String image) {
        log.info("Updating Product Image: {}", image);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));

        product.setImage(image);
        productRepository.save(product);

        return mapToProductResponse(product);
    }

    /**
     * Deletes a product by SKU code.
     *
     * @param skuCode the SKU code
     * @return a confirmation message
     */
    public String deleteProduct(String skuCode) {
        if (!inventoryClient.deleteProductFromInventory(skuCode)) {
            throw new RuntimeException("Product deletion failed!");
        }

        log.info("Deleting Product");
        productRepository.deleteBySkuCode(skuCode);
        return "Product Deleted";
    }

    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSkuCode(),
                product.getCategory(),
                product.getBrand(),
                product.getDescription(),
                product.getImage(),
                product.getPrice(),
                product.getUpdatedAt());
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSkuCode());
    }

    private InventoryResponse mapToInventoryResponse(Product product, InventoryResponse inventory) {
        return new InventoryResponse(
                product.getId(),
                product.getName(),
                product.getSkuCode(),
                product.getCategory(),
                product.getBrand(),
                product.getDescription(),
                product.getImage(),
                product.getPrice(),
                product.getUpdatedAt(),
                inventory != null ? inventory.quantity() : 0);
    }
}
