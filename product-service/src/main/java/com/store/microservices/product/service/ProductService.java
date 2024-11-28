package com.store.microservices.product.service;


import com.store.microservices.product.client.InventoryClient;
import com.store.microservices.product.dto.InventoryResponse;
import com.store.microservices.product.dto.ProductRequest;
import com.store.microservices.product.dto.ProductResponce;
import com.store.microservices.product.model.Product;
import com.store.microservices.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.origin.Origin;
import org.springframework.stereotype.Service;

import javax.lang.model.util.Elements;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;
    public ProductResponce createProduct(ProductRequest productRequest){

        Origin productBySkuCode = productRepository.findBySkuCode(productRequest.skuCode());
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
                product.getPrice());

    }
    public List<ProductResponce> getAllProducts(){
        log.info("Fetching All Products");
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductResponce(
                        product.getId(),
                        product.getName(),
                        product.getSkuCode(),
                        product.getCategory(),
                        product.getBrand(),
                        product.getDescription(),
                        product.getImage(),
                        product.getPrice()))
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
                product.getPrice());
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
        productRepository.save(product);
        return new ProductResponce(
                product.getId(),
                product.getName(),
                product.getSkuCode(),
                product.getCategory(),
                product.getBrand(),
                product.getDescription(),
                product.getImage(),
                product.getPrice());
    }

    public String deleteProduct(String productId) {
        log.info("Deleting Product");
        productRepository.deleteById(productId);
        return "Product Deleted";
    }






}
