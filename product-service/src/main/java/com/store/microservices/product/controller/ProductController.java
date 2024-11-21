package com.store.microservices.product.controller;

import com.store.microservices.product.dto.ProductRequest;
import com.store.microservices.product.dto.ProductResponce;
import com.store.microservices.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponce createProduct (@RequestBody ProductRequest productRequest){

        return productService.createProduct(productRequest);


    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponce> getAllProducts(){
        return productService.getAllProducts();
    }

    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponce getProductById(@PathVariable String productId){
        return productService.getProductById(productId);
    }

    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponce updateProduct(@PathVariable String productId, @RequestBody ProductRequest productRequest){
        System.out.println("Product ID: " + productId);
        return productService.updateProduct(productId, productRequest);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteProduct(@PathVariable String productId){
        return productService.deleteProduct(productId);
    }


}
