package com.store.microservices.product.service;


import com.store.microservices.product.client.*;
import com.store.microservices.product.dto.InventoryResponse;
import com.store.microservices.product.dto.CategoryRequest;
import com.store.microservices.product.dto.CategoryResponse;
import com.store.microservices.product.dto.ProductRequest;
import com.store.microservices.product.dto.ProductResponce;
import com.store.microservices.product.model.Category;
import com.store.microservices.product.model.Product;
import com.store.microservices.product.repository.CategoryRepository;
import com.store.microservices.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.origin.Origin;
import org.springframework.stereotype.Service;

import javax.lang.model.util.Elements;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;
    private final CategoryRepository categoryRepository;

    public ProductResponce createProduct(ProductRequest productRequest){
        log.info("Creating Product:{}",productRequest.skuCode());

        Origin productBySkuCode = productRepository.findBySkuCode(productRequest.skuCode());
        log.info("Product By SKU Code: {}", productBySkuCode);
        if(productBySkuCode != null){
            log.error("Product Already Exists!");
            throw new RuntimeException("Product Already Exists!");
        }

        if(!inventoryClient.addProductToInventory(productRequest.skuCode())){
            throw new RuntimeException("Product adding failed!");
        };

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
        return new ProductResponce(
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

    public class ProductAlreadyExistsException extends RuntimeException {
        public ProductAlreadyExistsException(String message) {
            super(message);
        }
    }


    public CategoryResponse createCategory(CategoryRequest categoryRequest){
        Category category = Category.builder()
                .name(categoryRequest.name())
                .skuCode(categoryRequest.skuCode())
                .build();
        categoryRepository.save(category);
        log.info("Category Created Successfully!");
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSkuCode()
               );

    }


    public List<InventoryResponse> getAllProducts() {
        log.info("Fetching All Products");

        // Fetch inventory data
        List<InventoryResponse> inventoryList = inventoryClient.getAllInventory();
        log.info("Fetched Inventory Data");

        // Build a map for quick lookup by skuCode
        Map<String, InventoryResponse> inventoryMap = inventoryList.stream()
                .collect(Collectors.toMap(InventoryResponse::skuCode, inventory -> inventory));

        // Fetch all products and map to ProductResponce
        return productRepository.findAll()
                .stream()
                .map(product -> {
                    InventoryResponse inventory = inventoryMap.get(product.getSkuCode());

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
                            inventory != null ? inventory.quantity() : 0 // Add quantity or default to 0
                    );
                })
                .toList();
    }



    public List<CategoryResponse> getAllCategories(){
        log.info("Fetching All Categories");
        return categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getSkuCode()
                      ))
                .toList();
    }

    public ProductResponce getProductById(String productId) {
        log.info("Fetching Product By Id");
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));
        return new ProductResponce(
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


    public InventoryResponse getProductQuantity(String skuCode) {
        log.info("Fetching Product Quantity");
        Integer quantity = inventoryClient.getProductQuantity(skuCode);
        Origin productBySkuCode = productRepository.findBySkuCode(skuCode);
        Product product = productRepository
                .findById(productBySkuCode.toString())
                .orElseThrow(() -> new RuntimeException("Product Not Found"));
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
                quantity);
    }

    public ProductResponce updateProduct(String productId, ProductRequest productRequest) {
        log.info("Updating Product");
        Product product = productRepository
                .findById(productId)
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
        return new ProductResponce(
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

    public ProductResponce updateProductImage(String productId, String image) {
        log.info("Updating Product Image : {}",image);
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));
        product.setImage(image);
        productRepository.save(product);
        return new ProductResponce(
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

    public String deleteProduct(String skuCode) {
        if(!inventoryClient.deleteProductFromInventory(skuCode)){
            log.info("Product deletion failed!");
            throw new RuntimeException("Product deletion failed!");
        }

        log.info("Deleting Product");
        productRepository.deleteBySkuCode(skuCode);
        return "Product Deleted";
    }






}
